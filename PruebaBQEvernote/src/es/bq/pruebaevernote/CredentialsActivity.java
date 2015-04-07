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

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.evernote.client.android.AsyncLinkedNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;

import es.bq.pruebaevernote.utils.DataUtils;

import java.util.List;

public class CredentialsActivity extends Activity {

	protected EvernoteSession mEvernoteSession;
	protected final int DIALOG_PROGRESS = 101;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mEvernoteSession = EvernoteSession.getInstance(this,
				DataUtils.CONSUMER_KEY, DataUtils.CONSUMER_SECRET,
				DataUtils.EVERNOTE_SERVICE,
				DataUtils.SUPPORT_APP_LINKED_NOTEBOOKS);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			return new ProgressDialog(CredentialsActivity.this);
		}
		return super.onCreateDialog(id);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_PROGRESS:
			((ProgressDialog) dialog).setIndeterminate(true);
			dialog.setCancelable(false);
			((ProgressDialog) dialog)
					.setMessage(getString(R.string.esdk__loading));
		}
	}

	protected void invokeOnAppLinkedNotebook(
			final OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>> callback) {
		try {
			// We need to get the one and only linked notebook
			mEvernoteSession
					.getClientFactory()
					.createNoteStoreClient()
					.listLinkedNotebooks(
							new OnClientCallback<List<LinkedNotebook>>() {
								@Override
								public void onSuccess(
										List<LinkedNotebook> linkedNotebooks) {
									// We should only have one linked notebook
									if (linkedNotebooks.size() != 1) {
										callback.onException(new Exception(
												"Not single linked notebook"));
									} else {
										final LinkedNotebook linkedNotebook = linkedNotebooks
												.get(0);
										mEvernoteSession
												.getClientFactory()
												.createLinkedNoteStoreClientAsync(
														linkedNotebook,
														new OnClientCallback<AsyncLinkedNoteStoreClient>() {
															@Override
															public void onSuccess(
																	AsyncLinkedNoteStoreClient asyncLinkedNoteStoreClient) {
																callback.onSuccess(new Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>(
																		asyncLinkedNoteStoreClient,
																		linkedNotebook));
															}

															@Override
															public void onException(
																	Exception exception) {
																callback.onException(exception);
															}
														});
									}
								}

								@Override
								public void onException(Exception exception) {
									callback.onException(exception);
								}
							});
		} catch (TTransportException exception) {
			callback.onException(exception);
		}
	}

	protected void createNoteInAppLinkedNotebook(final Note note,
			final OnClientCallback<Note> createNoteCallback) {
		showDialog(DIALOG_PROGRESS);
		invokeOnAppLinkedNotebook(new OnClientCallback<Pair<AsyncLinkedNoteStoreClient, LinkedNotebook>>() {
			@Override
			public void onSuccess(
					final Pair<AsyncLinkedNoteStoreClient, LinkedNotebook> pair) {
				// Rely on the callback to dismiss the dialog
				pair.first.createNoteAsync(note, pair.second,
						createNoteCallback);
			}

			@Override
			public void onException(Exception exception) {
				Toast.makeText(getApplicationContext(),
						R.string.error_creating_notestore, Toast.LENGTH_LONG)
						.show();
				removeDialog(DIALOG_PROGRESS);
			}
		});
	}
}
