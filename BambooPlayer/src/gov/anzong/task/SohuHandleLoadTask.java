package gov.anzong.task;

import gov.anzong.bean.VideoDetialData;
import gov.anzong.bean.VideoParseJson;
import gov.anzong.fragment.ProgressDialogFragment;
import gov.anzong.mediaplayer.R;
import gov.anzong.mediaplayer.VideoActivity;
import gov.anzong.receiveintent.ReceiveIntentURLActivity;
import gov.anzong.util.FunctionUtil;
import gov.anzong.util.HttpUtil;
import gov.anzong.util.StringUtil;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.widget.Toast;

public class SohuHandleLoadTask extends AsyncTask<String, Integer, String> {

	final String origurl, from;
	String title = "";
	String m3u8Url = "";
	final FragmentActivity context;
	static final String dialogTag = "load";
	int errorcode = -1;// 0��������,1�������,2��������

	public SohuHandleLoadTask(String origurl, String from,
			FragmentActivity context) {
		super();
		this.origurl = origurl;
		this.from = from;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		FunctionUtil.createdialog(context, dialogTag);
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String result) {
		FunctionUtil.closedialog(context, dialogTag);
		if (StringUtil.isEmpty(m3u8Url)) {
			String errorinfo = "";
			switch (errorcode) {// 0��������,1�������,2��������
			case 0:
				errorinfo = "��������";
				break;
			case 1:
				errorinfo = "�������";
				break;
			case 2:
				errorinfo = "��������";
				break;
			case -1:
			default:
				errorinfo = "δ֪����";
				break;
			}
			Toast.makeText(context, errorcode, Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(origurl));
			boolean isIntentSafe = context.getPackageManager()
					.queryIntentActivities(intent, 0).size() > 0;
			if (isIntentSafe) {
				Intent chooser = Intent.createChooser(intent, "������,��ѡ�������򿪷�ʽ:");
				context.startActivityForResult(chooser, 123);
			}
		} else {
			if (result != null) {
				title = result;
			} else {
				title = "�Ѻ���Ƶ";
			}
			VideoActivity.openVideo(context, Uri.parse(m3u8Url), title);
			this.onCancelled();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled(String result) {
		this.onCancelled();
	}

	@Override
	protected void onCancelled() {
		if (context != null) {
			context.finish();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		String uri = params[0];
		if (StringUtil.isEmpty(uri)) {
			errorcode = 0;
			return null;
		}
		final String htmlString = HttpUtil.iosGetHtml(uri);
		if (StringUtil.isEmpty(htmlString)) {
			errorcode = 1;
			return null;
		}
		final String id = StringUtil.getStringBetween(htmlString, 0, "vid=\"",
				"\";").result;
		if (StringUtil.isEmpty(id)) {
			errorcode = 2;
			return null;
		}
		m3u8Url = "http://hot.vrs.sohu.com/ipad" + id + ".m3u8";

		uri = FunctionUtil.uribase64(origurl);
		String js = "";
		for (int i = 0; i < 3; i++) {
			js = HttpUtil.iosGetHtml(uri);
			if (!StringUtil.isEmpty(js)) {
				i = 10;
			}
		}
		if (StringUtil.isEmpty(js)) {
			return null;
		}
		try {
			VideoDetialData[] jsondata = VideoParseJson.parseRead(js);
			if (FunctionUtil.isjsondataempty(jsondata)) {
				return null;
			}
			String title2 = jsondata[0].title;
			String sitename = jsondata[0].site;
			title = FunctionUtil.videotitle(title2, sitename, from);
			return jsondata[0].title;
		} catch (Exception e) {
			return null;
		}
	}

}
