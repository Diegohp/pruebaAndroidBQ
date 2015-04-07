package es.bq.pruebaevernote;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

	private Button sendcredentialsButton;
	
	private Button buttonAddNote;
	
	private Button buttonSortNotes;

	private String username;

	private String password;

	private Context mContext;

	private String authTokenAfterConnection;
	
	private int idButtonAddNote,idButtonSortNotes, idButtonLogout;

	// private SharedPreferences mSharedPreferences;

	private ArrayAdapter mAdapter;

	private ListView mListView;

	private ArrayList<String> listNotes = new ArrayList<String>();

	private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
//			Intent intentToShowNotes = new Intent(mContext,
//					ShowNotesActivity.class);
//			// intentToShowNotes.putExtra();
//			startActivity(intentToShowNotes);

			// switch(position) {
			// case 0:
			// startActivity(new Intent(getApplicationContext(),
			// ImagePicker.class));
			// break;
			// case 1:
			// startActivity(new Intent(getApplicationContext(),
			// SimpleNote.class));
			// break;
			// case 2:
			// startActivity(new Intent(getApplicationContext(),
			// SearchNotes.class));
			// }
		}
	};
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v.getId() == buttonAddNote.getId()){
				startActivity(new Intent(getApplicationContext(),
						 AddNoteActivity.class));
			} else if (v.getId() == buttonSortNotes.getId()){
				
			} else	{
				
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

		mEvernoteSession = EvernoteSession.getInstance(mContext,
				DataUtils.CONSUMER_KEY, DataUtils.CONSUMER_SECRET,
				DataUtils.EVERNOTE_SERVICE, false);
		mEvernoteSession.authenticate(mContext);

		mAdapter = new ArrayAdapter<String>(this, R.layout.list_notes,
				listNotes);

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(mItemClickListener);
		
		buttonAddNote.setOnClickListener(mOnClickListener);
		
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	}

	@Override
	public void onResume() {
		super.onResume();
		// updateAuthUi();
	}

	// private void updateAuthUi() {
	// //show login button if logged out
	// mLoginButton.setEnabled(!mEvernoteSession.isLoggedIn());
	//
	// //Show logout button if logged in
	// // mLogoutButton.setEnabled(mEvernoteSession.isLoggedIn());
	//
	// //disable clickable elements until logged in
	// mListView.setEnabled(mEvernoteSession.isLoggedIn());
	// }

	public void login(View view) {
		mEvernoteSession.authenticate(this);
	}

	public void logout(View view) {
		try {
			mEvernoteSession.logOut(this);
		} catch (InvalidAuthenticationException e) {
			e.printStackTrace();
		}
		// Log.e(LOGTAG, "Tried to call logout with not logged in", e);
		// }
		// updateAuthUi();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		// Update UI when oauth activity returns result
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				try {
					listNotebooks();
				} catch (TTransportException e) {
					e.printStackTrace();
				}
				// showNotesFromUser(data);
			} else {
				Toast.makeText(mContext, R.string.auth_log_error,
						Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	private void showNotesFromUser(Intent data) {

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
							// Log.e(LOGTAG, "Error retrieving notebooks",
							// exception);
						}
					});
		}
	}
	
//	public void listNotes() throws TTransportException {
//		if (mEvernoteSession.isLoggedIn()) {
//			mEvernoteSession.getClientFactory().createNoteStoreClient().
//					.
//	}

	 public void addNotesItems(String names) {
	
//	 listNotes.add("Clicked : "+clickCounter++);
		 listNotes.add(names);
		 mAdapter.notifyDataSetChanged();
	 }
	 
}
