package es.bq.pruebaevernote;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import es.bq.pruebaevernote.utils.DataUtils;

public class LoginScreenActivity extends Activity {

	private EvernoteSession mEvernoteSession;

	private EditText usernamefield;

	private EditText passwordfield;

	private Button logoutbutton;
	
	private Button buttonAddNote;
	
	private Button buttonSortNotes;

	private String username;

	private String password;

	private Context mContext;

	private String authTokenAfterConnection;
	
	private int idButtonAddNote,idButtonSortNotes, idButtonLogout;

	private ArrayAdapter mAdapter;

	private ListView mListView;

	private ArrayList<String> listNotes = new ArrayList<String>();

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		}
	};
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v.getId() == buttonAddNote.getId()){
				startActivity(new Intent(getApplicationContext(),
						 AddNoteActivity.class));
			} else if (v.getId() == logoutbutton.getId()){
				 try {
					mEvernoteSession.logOut(mContext);
					finish();
				} catch (InvalidAuthenticationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		mContext = this;
		
		mListView = (ListView) findViewById(R.id.notes_list);
		
		buttonAddNote = (Button)findViewById(R.id.add_note);
		
		buttonSortNotes = (Button)findViewById(R.id.filter); 
		
		logoutbutton = (Button)findViewById(R.id.send_credentials);

		mEvernoteSession = EvernoteSession.getInstance(mContext,
				DataUtils.CONSUMER_KEY, DataUtils.CONSUMER_SECRET,
				DataUtils.EVERNOTE_SERVICE, false);
		mEvernoteSession.authenticate(mContext);

		mAdapter = new ArrayAdapter<String>(this, R.layout.list_notes,
				listNotes);

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);
		
		buttonAddNote.setOnClickListener(mOnClickListener);
		logoutbutton.setOnClickListener(mOnClickListener);
		
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void login(View view) {
		mEvernoteSession.authenticate(this);
	}

	public void logout(View view) {
		try {
			mEvernoteSession.logOut(this);
		} catch (InvalidAuthenticationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				try {
					listNotebooks();
				} catch (TTransportException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(mContext, R.string.auth_log_error,
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	public void listNotebooks() throws TTransportException {
		if (mEvernoteSession.isLoggedIn()) {
			mEvernoteSession.getClientFactory().createNoteStoreClient()
					.listNotebooks(new OnClientCallback<List<Notebook>>() {
						
						@Override
						public void onSuccess(final List<Notebook> notebooks) {
							List<String> namesList = new ArrayList<String>(
									notebooks.size());
							for (Notebook notebook : notebooks) {
								namesList.add(notebook.getName());
							}
							String notebookNames = TextUtils.join(", ",
									namesList);
							Toast.makeText(
									getApplicationContext(),
									notebookNames
											+ " notebooks have been retrieved",
									Toast.LENGTH_LONG).show();
							
						}

						@Override
						public void onException(Exception exception) {
							exception.printStackTrace();
						}
					});
		}
	}
	

	 public void addNotesItems(String names) {
		 listNotes.add(names);
		 mAdapter.notifyDataSetChanged();
	 }
	 
}
