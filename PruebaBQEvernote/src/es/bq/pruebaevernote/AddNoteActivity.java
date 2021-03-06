/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package es.bq.pruebaevernote;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

public class AddNoteActivity extends CredentialsActivity {

	private static final String LOGTAG = "SimpleNote";

	private EditText mEditTextTitle;
	private EditText mEditTextContent;
	private Button mBtnSave;
	private Button mBtnSelect;

	private String mSelectedNotebookGuid;

	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@Override
		public void onSuccess(Note note) {
			Toast.makeText(getApplicationContext(), R.string.note_saved,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}

		@Override
		public void onException(Exception exception) {
			Log.e(LOGTAG, "Error saving note", exception);
			Toast.makeText(getApplicationContext(), R.string.error_saving_note,
					Toast.LENGTH_LONG).show();
			removeDialog(DIALOG_PROGRESS);
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_note);

		mEditTextTitle = (EditText) findViewById(R.id.new_note_title);
		mEditTextContent = (EditText) findViewById(R.id.new_note_content);
		mBtnSelect = (Button) findViewById(R.id.select_button);
		mBtnSave = (Button) findViewById(R.id.save_button);
	}

	public void saveNote(View view) {
		String title = mEditTextTitle.getText().toString();
		String content = mEditTextContent.getText().toString();
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
			return;
		}

		Note note = new Note();
		note.setTitle(title);

		note.setContent(EvernoteUtil.NOTE_PREFIX + content
				+ EvernoteUtil.NOTE_SUFFIX);

		if (!mEvernoteSession.getAuthenticationResult().isAppLinkedNotebook()) {
			if (!TextUtils.isEmpty(mSelectedNotebookGuid)) {
				note.setNotebookGuid(mSelectedNotebookGuid);
			}
			showDialog(DIALOG_PROGRESS);
			try {
				mEvernoteSession.getClientFactory().createNoteStoreClient()
						.createNote(note, mNoteCreateCallback);
			} catch (TTransportException exception) {
				Log.e(LOGTAG, "Error creating notestore", exception);
				Toast.makeText(getApplicationContext(),
						R.string.error_creating_notestore, Toast.LENGTH_LONG)
						.show();
				removeDialog(DIALOG_PROGRESS);
			}
		} else {
			super.createNoteInAppLinkedNotebook(note, mNoteCreateCallback);
		}
	}

	public void selectNotebook(View view) {
		if (mEvernoteSession.isAppLinkedNotebook()) {
			return;
		}

		try {
			mEvernoteSession.getClientFactory().createNoteStoreClient()
					.listNotebooks(new OnClientCallback<List<Notebook>>() {
						int mSelectedPos = -1;

						@Override
						public void onSuccess(final List<Notebook> notebooks) {
							CharSequence[] names = new CharSequence[notebooks
									.size()];
							int selected = -1;
							Notebook notebook = null;
							for (int index = 0; index < notebooks.size(); index++) {
								notebook = notebooks.get(index);
								names[index] = notebook.getName();
								if (notebook.getGuid().equals(
										mSelectedNotebookGuid)) {
									selected = index;
								}
							}

							AlertDialog.Builder builder = new AlertDialog.Builder(
									AddNoteActivity.this);

							builder.setSingleChoiceItems(names, selected,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mSelectedPos = which;
										}
									})
									.setPositiveButton(
											R.string.dialog_positive_button,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													if (mSelectedPos > -1) {
														mSelectedNotebookGuid = notebooks
																.get(mSelectedPos)
																.getGuid();
													}
													dialog.dismiss();
												}
											}).create().show();
						}

						@Override
						public void onException(Exception exception) {
							Log.e(LOGTAG, "Error listing notebooks", exception);
							Toast.makeText(getApplicationContext(),
									R.string.error_listing_notebooks,
									Toast.LENGTH_LONG).show();
							removeDialog(DIALOG_PROGRESS);
						}
					});
		} catch (TTransportException exception) {
			Log.e(LOGTAG, "Error creating notestore", exception);
			Toast.makeText(getApplicationContext(),
					R.string.error_creating_notestore, Toast.LENGTH_LONG)
					.show();
			removeDialog(DIALOG_PROGRESS);
		}
	}
}
