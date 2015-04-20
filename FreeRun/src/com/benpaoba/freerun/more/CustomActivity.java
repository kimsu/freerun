package com.benpaoba.freerun.more;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.benpaoba.freerun.R;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

public class CustomActivity extends Activity {

	private ListView mListView;
	private FeedbackAgent mAgent;
	private Conversation mComversation;
	private Context mContext;
	private ReplyAdapter adapter;
	private List<Reply> mReplyList;
	private Button sendBtn;
	private EditText inputEdit;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private final String TAG = CustomActivity.class.getName();

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			mSwipeRefreshLayout.setRefreshing(false);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom);
		mContext = this;
	
		
		initView();
		mAgent = new FeedbackAgent(this);
		mComversation = mAgent.getDefaultConversation();
		
		//Log.d(TAG, "mComversation -- " + mComversation.getId());
		adapter = new ReplyAdapter();
		mListView.setAdapter(adapter);
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Reply pp = (Reply)adapter.getItem(arg2);
				mComversation.getReplyList().remove(pp);
				adapter.notifyDataSetChanged();
				return false;
			}
		});

	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.fb_reply_list);
		sendBtn = (Button) findViewById(R.id.fb_send_btn);
		inputEdit = (EditText) findViewById(R.id.fb_send_content);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fb_reply_refresh);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = inputEdit.getText().toString();
				inputEdit.getEditableText().clear();
				Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
				if (!TextUtils.isEmpty(content)) {
					mComversation.addUserReply(content);
					sync();
				}

			}
		});
		
		mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				sync();
				
			}
		});
	}

	private void sync() {
		// 数据同步
		mComversation.sync(new SyncListener() {

			@Override
			public void onSendUserReply(List<Reply> replyList) {

			}

			@Override
			public void onReceiveDevReply(List<Reply> replyList) {
				mSwipeRefreshLayout.setRefreshing(false);
				if (replyList == null || replyList.size() < 1) {
					return;
				}
				// 该replyList为开发者新的回复（不包含本地的数据）
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						adapter.notifyDataSetChanged();						
					}
				});
			}
		});
	}

	class ReplyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mComversation.getReplyList().size();
		}

		@Override
		public Object getItem(int arg0) {
			return mComversation.getReplyList().get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			TextView tv = new TextView(mContext);
			tv.setHeight(100);
			tv.setTextSize(18);
			Reply reply = mComversation.getReplyList().get(arg0);
			String content;
			if(Reply.TYPE_DEV_REPLY.endsWith(reply.type)){
				tv.setGravity(Gravity.RIGHT);
				content = reply.content + ": 开发者";
			}else{
				content = ">" + reply.content;
			}
			tv.setText(content);
			return tv;
		}

	}
}
