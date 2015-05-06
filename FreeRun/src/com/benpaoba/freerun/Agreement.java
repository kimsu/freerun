package com.benpaoba.freerun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Agreement extends Activity {
	private RadioGroup agreement = null;
	private RadioButton agree = null;
	private RadioButton disagree = null;
	private Button next = null;
	private Button goback = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agreement);
		init();
		initListener();
	}
	/**
	 * ҳ��Ԫ�س�ʼ��
	 */
	private void init (){
		this.agreement = (RadioGroup)this.findViewById(R.id.agreement);
		this.agree = (RadioButton)this.findViewById(R.id.agree);
		this.disagree = (RadioButton)this.findViewById(R.id.disagree);
		this.next = (Button)this.findViewById(R.id.next);
		this.goback = (Button)this.findViewById(R.id.gobackInAgreement);
	}
	/**
	 * ��agreement�ϵ�ҳ��Ԫ�����ü���
	 */
	private void initListener(){
		this.agreement.setOnCheckedChangeListener(new RadioGroupAgreement());
		this.next.setOnClickListener(new ButtonNext());
		this.goback.setOnClickListener(new ButtonGoback());
	}
	/*
	 * ����RadioGroup--agreement�ļ���
	 */
	private class RadioGroupAgreement implements RadioGroup.OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == agree.getId()) {
				next.setEnabled(true);
			}else{
				next.setEnabled(false);
			}
		}		
	}
	/*
	 * ����next�ļ���
	 */
	private class ButtonNext implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
				Intent agreementRregister = new Intent(Agreement.this, Register.class);
				startActivity(agreementRregister);
				finish();
		}
	}
	/*
	 * ����goback����
	 */
	private class ButtonGoback implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent agreementLogin = new Intent(Agreement.this, WlecomActivity.class);
			startActivity(agreementLogin);
			finish();
		}		
	}
}

