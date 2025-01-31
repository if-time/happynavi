package com.trackersurvey.fragment;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.utils.ThreadManager;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.trackersurvey.adapter.ListBaseAdapter;
import com.trackersurvey.adapter.ListBaseAdapter3;
import com.trackersurvey.adapter.ListBaseAdapter3.DeleCommListener;
import com.trackersurvey.db.PhotoDBHelper;
import com.trackersurvey.db.PointOfInterestDBHelper;
import com.trackersurvey.db.TraceDBHelper;
import com.trackersurvey.entity.GpsData;
import com.trackersurvey.entity.ListItemData;
import com.trackersurvey.entity.PointOfInterestData;
import com.trackersurvey.entity.StepData;
import com.trackersurvey.entity.TraceData;
import com.trackersurvey.happynavi.CommentActivity;
import com.trackersurvey.happynavi.DrawPath;
import com.trackersurvey.happynavi.LoginActivity;
import com.trackersurvey.happynavi.R;
import com.trackersurvey.happynavi.SetParameter;
import com.trackersurvey.happynavi.TabHost_Main;
import com.trackersurvey.helper.AMapUtil;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.GsonHelper;
import com.trackersurvey.helper.ShareToWeChat;
import com.trackersurvey.helper.TextMoveLayout;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostDeleteTrail;
import com.trackersurvey.httpconnection.PostEndTrail;
import com.trackersurvey.httpconnection.PostGpsData;
import com.trackersurvey.httpconnection.PostPointOfInterestData;
import com.trackersurvey.httpconnection.PostPointOfInterestDataEn;
import com.trackersurvey.httpconnection.PostTrailDetail;
import com.trackersurvey.model.MyCommentModel;
import com.trackersurvey.photoview.SlideListView;
import com.trackersurvey.service.CommentUploadService;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ShowTraceFragment extends Fragment implements OnClickListener, OnMapClickListener, OnMapLoadedListener,
		OnRouteSearchListener, OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter {
	private MapView mapView;
	private AMap aMap;
	private TextView distance;
	private TextView duration;
	private TextView time;
	private TextView speed;
	private TextView step;
	private TextView calorie;

	private String stepstr = "--";// 从intent获取
	private Polyline polyline;
	// private Circle circle;// 标记网络定位点
	private TraceData trailobj;
	private PointOfInterestData behaviourData,durationData, partnerNumData, relationData;
	private ProgressDialog proDialog = null;
	private List<GpsData> traces = new ArrayList<GpsData>();
	private List<GpsData> linkTraces = new LinkedList<GpsData>();
	private StepData stepdata = new StepData();
	private List<LatLng> points = new ArrayList<LatLng>();
	private List<LatLng> linkPoints = new LinkedList<LatLng>();
	private List<Integer> start = new ArrayList<Integer>();
	private List<Integer> end = new ArrayList<Integer>();
	private List<Integer> selectid = new ArrayList<Integer>();// 被过滤的坐标
	private int s_id = 0;// selectid的下标 用于连接步行规划起点终点不可达的情况
	private List<LatLng> NetPoints = new ArrayList<LatLng>();// 网络定位点
	private List<LatLng> MarkPoints = new ArrayList<LatLng>();// 标注点的经纬度
	private List<GpsData> keyPoints = new ArrayList<GpsData>();// 显示时间的关键点
	private List<Long> arrayMillSeconds = new ArrayList<Long>();
	private List<Marker> arrayMarker = new ArrayList<Marker>();
	private ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

	private DriveRouteResult driveRouteResult;// 驾车模式查询结果
	private WalkRouteResult walkRouteResult;// 步行模式查询结果
	private RouteSearch routeSearch;
	private String URL_GETTRAIL = null;
	private String URL_ENDTRAIL = null;
	private String URL_GPSDATA = null;
	private String URL_GETPOI = null;
	public final String UPDATEUI_ACTION = "android.intent.action.UPDATEUI_RECEIVER";// 给ShowPoiFragment发的广播
	private final String REFRESH_ACTION = "android.intent.action.REFRESH_RECEIVER";// 给TraceListActivity发的广播

	private boolean isOnline;
	private boolean canShare1 = false;
	private boolean canShare2 = false; // 本地轨迹上传成功才能分享
	private boolean isTimeLine = false;// false 分享给好友 true分享到朋友圈

	private TraceDBHelper helper = null;
	
	private Cursor cursor = null;
	
	private PointOfInterestDBHelper helper2 = null;
	
	private int maptype = 0;
	public static MyCommentModel myComment;// 用于获取标记信息

	private Context context;
	/**
	 * 以下是标注相关
	 */
	private LatLng markLatLng = null;// 选择的添加标注的位置
	private long startMills = 0;
	private long endMills = 0;
	private SeekBar seekbar = null;
	private ImageView mark_left, mark_right, addMarker, checkTrace, shareTrace;
	private String startTimeStr = "00:00:00";
	private String endTimeStr = "00:00:00";
	private TextView moveText, startTime, endTime;
	private PopupWindow mPopupWindow;

	/**
	 * 轨迹总时长
	 */
	private int totalSeconds = 0;
	private int currentProgress = 0;

	/**
	 * 屏幕宽度
	 */
	private int screenWidth;

	/**
	 * 自定义随着拖动条一起移动的空间
	 */
	private TextMoveLayout textMoveLayout;

	private ViewGroup.LayoutParams layoutParams;
	/**
	 * 托动条的移动步调
	 */
	private float moveStep = 0;
	private Marker selectedMarker = null;
	private final int REQUESTMARK = 0x11;
	// private CommentUploadService commentUploadService;
	// private Intent uploadService;
	// private boolean bound_upload=false;
	private SharedPreferences uploadCache;// 存储待上传的评论信息
	
	private Tencent mTencent;//用于qq空间分享
	private Bundle params;
	private MyIUilistener mIUilistener;//用于qq空间分享

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		int l = TabHost_Main.l;
		View view = null;
		
		// 中英文切换
		Resources resources = getResources();
		Configuration configure = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if(TabHost_Main.l==0){
			configure.locale = Locale.CHINESE;
		}
		if(TabHost_Main.l==1){
			configure.locale = Locale.ENGLISH;
		}
		if(TabHost_Main.l==2) {
			configure.locale = new Locale("cs", "CZ");
		}
		resources.updateConfiguration(configure, dm);
		
		
		if(l == 0){			
			view = inflater.inflate(R.layout.fragment_showpath, null);
		}
		if(l == 1){
			view = inflater.inflate(R.layout.fragment_showpath_en, null);
		}
		if(l == 2) {
			view = inflater.inflate(R.layout.fragment_showpath_en, null);
		}
		context = getActivity();
		mapView = (MapView) view.findViewById(R.id.show_amapView);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		moveText = new TextView(context);
		moveText.setBackgroundColor(Color.WHITE);
		moveText.setTextColor(Color.rgb(0, 161, 229));
		moveText.setTextSize(16);
		layoutParams = new ViewGroup.LayoutParams(screenWidth, 50);
		textMoveLayout = (TextMoveLayout) view.findViewById(R.id.textLayout);
		textMoveLayout.addView(moveText, layoutParams);
		moveText.layout(0, 20, screenWidth, 80);
		seekbar = (SeekBar) view.findViewById(R.id.seekbar);
		startTime = (TextView) view.findViewById(R.id.start_time);
		endTime = (TextView) view.findViewById(R.id.end_time);
		mark_left = (ImageView) view.findViewById(R.id.mark_left);
		mark_right = (ImageView) view.findViewById(R.id.mark_right);
		addMarker = (ImageView) view.findViewById(R.id.addmark);
		checkTrace = (ImageView) view.findViewById(R.id.checktraceinfo);
		shareTrace = (ImageView) view.findViewById(R.id.sharetrace);
		mTencent = Tencent.createInstance("1105447917", getContext());//用于qq空间分享

		// distance = (TextView) view.findViewById(R.id.distance_info);
		// duration = (TextView) view.findViewById(R.id.during_info);
		// time = (TextView) view.findViewById(R.id.time_info);
		// speed = (TextView) view.findViewById(R.id.speed_info);
		// step = (TextView) view.findViewById(R.id.step_info);
		// calorie = (TextView) view.findViewById(R.id.calorie_info);

		mark_left.setOnClickListener(this);
		mark_right.setOnClickListener(this);
		addMarker.setOnClickListener(this);
		checkTrace.setOnClickListener(this);
		shareTrace.setOnClickListener(this);
		mark_left.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				seekbar.setProgress(currentProgress - 100);
				return true;
			}
		});
		mark_right.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				seekbar.setProgress(currentProgress + 100);
				return true;
			}
		});
		routeSearch = new RouteSearch(context);
		routeSearch.setRouteSearchListener(this);
		helper = new TraceDBHelper(context);
		helper2 = new PointOfInterestDBHelper(context);//打开兴趣点数据库
		initAMap();
		initModel();
		// initBDMap();
		uploadCache = context.getSharedPreferences("uploadCache", Activity.MODE_PRIVATE);

		if (Common.url != null && !Common.url.equals("")) {

		} else {
			Common.url = getResources().getString(R.string.url);
		}
		URL_GETTRAIL = Common.url + "reqTraceHistory.aspx";
		URL_ENDTRAIL = Common.url + "reqTraceNo.aspx";
		URL_GPSDATA = Common.url + "upLocation.aspx";
		URL_GETPOI = Common.url + "requestInfo.aspx";
		if(l==0){			
			initPOI();//下载添加兴趣点下拉列表选项内容
		}
		if(l==1){
			initPOIEN();//英文版
		}
		return view;
	}

	/**
	 * 初始化AMap对象
	 */
	private void initAMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			aMap.getUiSettings().setZoomControlsEnabled(true);
			aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);// 设置缩放按钮在右侧中间位置

			aMap.getUiSettings().setCompassEnabled(true);
			aMap.getUiSettings().setScaleControlsEnabled(true);// 启用比例尺
			aMap.setOnMapLoadedListener(this);
			// mapView.setVisibility(View.GONE);
			aMap.setOnMapClickListener(this);
			aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
			aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
			aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式

		}
	}

	private void initPathData() {
		Intent intent = getActivity().getIntent();
		String trail = intent.getStringExtra("trail");
		stepstr = intent.getStringExtra("step");
		isOnline = intent.getBooleanExtra("isonline", true);
		// Log.i("trailadapter", "initPathData,"+trail);
		// Gson gson=new Gson();
		trailobj = GsonHelper.parseJson(trail, TraceData.class);
		Log.i("trailobj", "startTime:"+trailobj.getStartTime()+"endTime"+trailobj.getEndTime());
		if (!"--".equals(stepstr)) {
			stepdata = GsonHelper.parseJson(stepstr, StepData.class);
			stepstr = stepdata.getsteps() + "";
		}
		// distance.setText(Common.transformDistance(trailobj.getDistance()));
		// duration.setText(Common.transformTime(trailobj.getDuration()));
		// time.setText(trailobj.getStartTime());
		// speed.setText(Common.transformSpeed(trailobj.getDistance()/trailobj.getDuration())+"
		// "+getResources().getString(R.string.speedunit));
		// step.setText(stepstr);
		// calorie.setText(trailobj.getCalorie()+"
		// "+getResources().getString(R.string.calorieunit));

		if (isOnline) {
			showDialog(getResources().getString(R.string.tips_dlgtle_init),
					getResources().getString(R.string.tips_dlgmsg_inittrace));
			// Log.i("trailadapter", "在线获取");

			PostTrailDetail traildetail = new PostTrailDetail(handler, URL_GETTRAIL, trailobj.getUserID(),
					trailobj.getTraceNo(), Common.getDeviceId(context));
			traildetail.start();
			Log.i("traildetail", "traildetail:"+traildetail.toString());
		} else {
			// 从本地获取
			// Log.i("trailadapter", "从本地获取");
			traces = helper.queryfromGpsbytraceNo(trailobj.getTraceNo(), Common.getUserId(context));
			// Log.i("trailadapter", GsonHelper.toJson(traces));
			if (traces.size() > 0) {
				initLocation();
				if (Common.isNetConnected && trailobj.getSportType() != 5) {
					AMap_drawpath_optimize(points);

				} else {
					AMap_drawpath_normal(points);
					// BDMap_drawpath_normal(points);
				}

			} else {
				Toast.makeText(context, getResources().getString(R.string.tips_nodata), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void initSeekbar() {
		startTimeStr = traces.get(0).getcreateTime();
		String[] startTimes = startTimeStr.split(" ");
		Log.i("startTime", startTimeStr);
		Log.i("startTime", startTimes[1]);
		startTime.setText(startTimes[1]);
		endTimeStr = traces.get(traces.size() - 1).getcreateTime();
		String[] endTimes = endTimeStr.split(" ");
		endTime.setText(endTimes[1]);
		moveText.setText(startTimes[1]);
		// totalSeconds = totalSeconds(startTimeStr, endTimeStr);
		long duration = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try { // 计算时间差
			Date d1 = df.parse(startTimeStr);
			Date d2 = df.parse(endTimeStr);
			startMills = d1.getTime();
			endMills = d2.getTime();
			duration = endMills - startMills;
			totalSeconds = (int) (duration / 1000);
		} catch (Exception e) {
			totalSeconds = points.size();
		}

		seekbar.setEnabled(true);
		seekbar.setMax(totalSeconds);
		seekbar.setProgress(0);
		moveStep = (float) (((float) screenWidth / (float) totalSeconds) * 0.8);
		/**
		 * setListener
		 */
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
		// 添加默认的选中marker
		MarkerOptions options1 = new MarkerOptions();
		options1.position(points.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark));
		if (selectedMarker != null) {
			selectedMarker.remove();
			selectedMarker = null;
		}
		selectedMarker = aMap.addMarker(options1);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addmark:
			markLatLng = new LatLng(points.get(praseProgressToPosition(currentProgress)).latitude,
					points.get(praseProgressToPosition(currentProgress)).longitude);
			//从POI数据库中取数据
			ArrayList<String> behaviour = helper2.getBehaviour();
			ArrayList<String> duration = helper2.getDuration();
			ArrayList<String> partnerNum = helper2.getPartnerNum();
			ArrayList<String> relation = helper2.getRelation();
			//Log.i("duration", duration.toString());
			Intent intent = new Intent();
			intent.setClass(context, CommentActivity.class);
			intent.putExtra("longitude", markLatLng.longitude);
			intent.putExtra("latitude", markLatLng.latitude);
			intent.putExtra("altitude", traces.get(praseProgressToPosition(currentProgress)).getAltitude());
			intent.putExtra("placeName", "");
			intent.putExtra("createTime", traces.get(praseProgressToPosition(currentProgress)).getcreateTime());
			intent.putExtra("traceNo", traces.get(praseProgressToPosition(currentProgress)).gettraceNo());
			
			/*Resources r = context.getApplicationContext().getResources();
			Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
			+r.getResourcePackageName(R.drawable.ic_launcher_wx)+"/"
			+r.getResourceTypeName(R.drawable.ic_launcher_wx)+"/"
			+r.getResourceEntryName(R.drawable.ic_launcher_wx));
			String a = uri.toString();
			Log.i("1005", a);*/
			
			//传递POI数据（字符串数组）到添加兴趣点页面
			try {
				intent.putStringArrayListExtra("behaviour", behaviour);
				intent.putStringArrayListExtra("duration", duration);
				intent.putStringArrayListExtra("partnerNum", partnerNum);
				intent.putStringArrayListExtra("relation", relation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startActivityForResult(intent, REQUESTMARK);

			break;
		case R.id.checktraceinfo:
			if (mPopupWindow == null) {
				initPopupWindow();
			}
			if (!mPopupWindow.isShowing()) {
				mPopupWindow.showAsDropDown(checkTrace);
				mPopupWindow.update();
			}
			// ToastUtil.show(context, "待添加");
			break;
		case R.id.sharetrace:
			if(!Common.isNetConnected){
				ToastUtil.show(context, getResources().getString(R.string.tips_share_nonet1));
				return;
			}
			final Dialog dialog = new Dialog(context, R.style.NoTitleDialogStyle);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#09c7f7")));
			//dialog.setTitle(R.string.share_to);
			View contentView = LayoutInflater.from(context).inflate(
					R.layout.sharetowx, null);
			dialog.setContentView(contentView);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			ImageView close = (ImageView) contentView
					.findViewById(R.id.close_dialog);
			close.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			});
			
			ImageButton shareSession = (ImageButton) contentView
					.findViewById(R.id.share_wxsession);
			shareSession.setOnClickListener((OnClickListener) context);
			shareSession.setOnTouchListener((OnTouchListener) context);
			
			ImageButton shareTimeline = (ImageButton) contentView
					.findViewById(R.id.share_wxtinmeline);
			shareTimeline.setOnClickListener((OnClickListener) context);
			shareTimeline.setOnTouchListener((OnTouchListener) context);
			
			ImageButton shareQzone = (ImageButton) contentView
					.findViewById(R.id.share_qzone);
			shareQzone.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ShareToQZone();
				}
			});
			ImageButton shareQQ = (ImageButton) contentView.findViewById(R.id.share_qq);
			shareQQ.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					ShareToQQ();
				}
			});
			break;
		case R.id.mark_left:
			seekbar.setProgress(currentProgress - 10);
			break;
		case R.id.mark_right:
			seekbar.setProgress(currentProgress + 10);
			break;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 2:// 获取轨迹列表成功
				dismissDialog();
				if (msg.obj != null) {
					// Gson gson=new Gson();
					// traces=gson.fromJson(msg.obj.toString().trim(), new
					// TypeToken<ArrayList<GpsData>>(){}.getType());
					traces = GsonHelper.parseJsonToList(msg.obj.toString().trim(), GpsData.class);
					// Toast.makeText(context, "收到轨迹条数："+traces.size(),
					// Toast.LENGTH_SHORT).show();
					Log.i("traildetail", "traildetail"+msg.obj);
					if (traces.size() > 0) {
						initLocation();
						if (Common.isNetConnected && trailobj.getSportType() != 5) {
							AMap_drawpath_optimize(points);

						} else {
							AMap_drawpath_normal(points);
							// BDMap_drawpath_normal(points);
						}

					} else {
						Toast.makeText(context, getResources().getString(R.string.tips_nodata), Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(context, getResources().getString(R.string.tips_nodata), Toast.LENGTH_SHORT).show();
				}

				break;
			case 3:// 获取列表失败
				dismissDialog();
				Toast.makeText(context, getResources().getString(R.string.tips_initfail), Toast.LENGTH_SHORT).show();

				break;
			case 4:// 删除成功
				dismissDialog();

				ToastUtil.show(context, getResources().getString(R.string.tips_deletesuccess));
				Intent intent = new Intent();
				intent.setAction(REFRESH_ACTION);
				context.sendBroadcast(intent);
				getActivity().finish();
				break;
			case 5:// 删除失败,也要更新，因为本地数据库中如果本来有这条轨迹，那么会删掉
				dismissDialog();
				Intent intent2 = new Intent();
				intent2.setAction(REFRESH_ACTION);
				context.sendBroadcast(intent2);

				ToastUtil.show(context, getResources().getString(R.string.tips_deletefail));
				break;
			case 11:// 网络错误
				dismissDialog();
				Toast.makeText(context, getResources().getString(R.string.tips_netdisconnect), Toast.LENGTH_SHORT)
						.show();

				break;
			case 12:// 网络错误
				dismissDialog();
				Intent intent3 = new Intent();
				intent3.setAction(REFRESH_ACTION);
				context.sendBroadcast(intent3);

				ToastUtil.show(context, getResources().getString(R.string.tips_netdisconnect));
				break;
			}
		}
	};
	
	//传递给PostPointOfInterestData的handler1
	private Handler handler1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if(msg.obj!=null){
					String[] poiStr = msg.obj.toString().trim().split("#");
					String[] behaviourStr = poiStr[1].split("[$]");
					String[] durationStr = poiStr[0].split("[$]");
					String[] partnerNumStr = poiStr[2].trim().split("[$]");
					String[] relationStr = poiStr[3].trim().split("[$]");
					behaviourData = new PointOfInterestData();
					durationData = new PointOfInterestData();
					partnerNumData = new PointOfInterestData();
					relationData = new PointOfInterestData();
					//Log.i("poiStr", poiStr[0]);
					helper2.delete();
					try {
						for(int i = 0;i<behaviourStr.length;i++){
							behaviourData.setKey(i);
							behaviourData.setValue(behaviourStr[i]);
							helper2.insertBehaviour(behaviourData);
						}
						for(int i = 0;i<durationStr.length;i++){
							durationData.setKey(i);
							durationData.setValue(durationStr[i]);
							//将数据插入到POI数据库中
							helper2.insertDuration(durationData);
						}
						for(int i = 0;i<partnerNumStr.length;i++){
							partnerNumData.setKey(i);
							partnerNumData.setValue(partnerNumStr[i]);
							helper2.insertPartnerNum(partnerNumData);
						}
						for(int i = 0;i<relationStr.length;i++){
							relationData.setKey(i);
							relationData.setValue(relationStr[i]);
							helper2.insertPartnerRelation(relationData);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				break;
			case 1:

				break;
			}
		}
	};

	public void initLocation() {
		for (int i = 0; i < traces.size(); i++) {
			LatLng point = new LatLng(traces.get(i).getLatitude(), traces.get(i).getLongitude());
			// LatLng 经纬度保留小数点后6位
			// Log.i("trailadapter",
			// "第"+i+"个记录：("+traces.get(i).getLongitude()+","+traces.get(i).getLatitude()+")");
			// Log.i("trailadapter",
			// "第"+i+"个记录：("+point.longitude+","+point.latitude+")");

			points.add(point);
			if (traces.get(i).getAltitude() == 0) {
				NetPoints.add(point);
			}
		}

		locationFilter();
		initSeekbar();
		initMarker();
	}

	/**
	 * 初始化标注信息
	 */
	public void initModel() {
		myComment = new MyCommentModel(context, "mark");
		// 监听下载评论
		myComment.setmDownComment(new MyCommentModel.DownCommentListener() {

			@Override
			public void onCommentDownload(int msg) {
				// TODO Auto-generated method stub
				if (msg == 0) {
					drawMarker();
				} else if (msg == 10) {
					Toast.makeText(context, R.string.tips_netdisconnect, Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(context, R.string.tips_postfail, Toast.LENGTH_SHORT).show();
				}

			}
		});
		myComment.setmDeleteComment(new MyCommentModel.DeleteCommentListener() {
			@Override
			public void onCommentDeleted(int msg) {
				// TODO Auto-generated method stub
				switch (msg) {
				case 0: {
					initMarker();
					// Toast.makeText(context,R.string.tips_deletesuccess,
					// Toast.LENGTH_SHORT).show();
					break;
				}
				case 1: {
					Toast.makeText(context, R.string.tips_deletefail_dberror, Toast.LENGTH_SHORT).show();
					break;
				}
				case 2: {
					Toast.makeText(context, R.string.tips_postfail, Toast.LENGTH_SHORT).show();
					break;
				}
				case 3: {
					Toast.makeText(context, R.string.tips_postfail, Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					break;
				}
			}
		});

	}

	private void initMarker() {
		myComment.setTimeRegion(trailobj.getStartTime(), trailobj.getEndTime());
		myComment.initMarkerItemsFromDB();
		
		//创建或打开数据库，如果数据库中没有数据就从服务器请求兴趣点数据
		PhotoDBHelper phelper = new PhotoDBHelper(context, PhotoDBHelper.DBREAD);
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		String from = traces.get(0).getcreateTime();
		String to = traces.get(traces.size() - 1).getcreateTime();
		cursor = phelper.selectEvent(null, PhotoDBHelper.COLUMNS_UE[10] + "="
				+ Common.getUserId(context)+" and datetime("
						+ PhotoDBHelper.COLUMNS_UE[0] + ") between '"+from+
						"' and '"+to+"'", null, null, null, null);
		//如果数据库中没有数据，就从服务器中请求兴趣点数据
		if(Common.isNetConnected&&(cursor.getCount()==0)){
			myComment.initMarkerItemsOnline();
		}
		
		drawMarker();
		Intent intent = new Intent();
		intent.setAction(UPDATEUI_ACTION);
		context.sendBroadcast(intent);
	}

	public void refreshMarker() {
		if (Common.isNetConnected) {
			// 网络连接，更新本地标注信息
			Log.i("mark", "更新标注");  
			Log.i("trailobj", "trailobj:"+trailobj);
			myComment.initMarkerItemsOnline();
			//drawMarker();
			Log.i("itemsss", "ShowTraceFragment:"+myComment.getItems().toString());
		} else {
			ToastUtil.show(context, getResources().getString(R.string.tips_netdisconnect));
		}
	}

	private void drawMarker() {
		items = myComment.getItems();
		List<Long> arryMarkerTime = new ArrayList<Long>();

		for (int i = 0; i < items.size(); i++) {
			// 添加标注marker的时间
			arryMarkerTime.add(praseStrToMillsecond(((ListItemData) items.get(i).get("listItem")).getTime()));

		}
		Log.i("mark", "items size " + items.size() + " marker size " + arryMarkerTime.size());
		for (int i = 0; i < arrayMarker.size(); i++) {
			arrayMarker.get(i).remove();
		}
		arrayMarker.clear();
		MarkPoints.clear();
		for (int i = 0; i < arryMarkerTime.size(); i++) {
			LatLng markLatLng = points.get(praseTimeToPosition(arryMarkerTime.get(i)));
			MarkPoints.add(markLatLng);
			arrayMarker.add(aMap.addMarker(new MarkerOptions().position(markLatLng)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)).title("mark")
					.anchor(0.5f, 0.5f)));

		}
		Log.i("arrayMarker", "arryMarkerTime.size = "+arryMarkerTime.size());
	}

	public void locationFilter() {
		Log.i("trailadapter", "过滤前坐标数：" + points.size());

		if (points.size() > 3) {
			for (int i = 0; i < points.size() - 2; i++) {
				for (int j = i + 1; j < points.size(); j++) {
					if (points.get(i).longitude == points.get(j).longitude
							&& points.get(i).latitude == points.get(j).latitude) {
						boolean have = false;
						for (int k = 0; k < selectid.size(); k++) {
							if (j == selectid.get(k)) {
								// 已存在
								have = true;
								break;
							}
						}
						if (!have) {
							selectid.add(j);
						}
					}
				}
				// float dis=AMapUtils.calculateLineDistance(currentLatlng,
				// lastLatlng);
				float d12 = AMapUtils.calculateLineDistance(points.get(i), points.get(i + 1));
				float d23 = AMapUtils.calculateLineDistance(points.get(i + 1), points.get(i + 2));
				float d13 = AMapUtils.calculateLineDistance(points.get(i), points.get(i + 2));
				if (d12 > (d13 * 5) && d23 > (d13 * 5) || d12 < 5.0) {
					boolean have = false;
					for (int k = 0; k < selectid.size(); k++) {
						if (i + 1 == selectid.get(k)) {
							// 已存在
							have = true;
							break;
						}
					}
					if (!have) {
						selectid.add(i + 1);
					}
				}
			}
			Log.i("trailadapter", "排序前：" + Arrays.toString(selectid.toArray()));
			Collections.sort(selectid);
			Log.i("trailadapter", "排序后：" + Arrays.toString(selectid.toArray()));
			for (int i = selectid.size() - 1; i >= 0; i--) {
				int deleteid = selectid.get(i);
				points.remove(deleteid);
				traces.remove(deleteid);

			}
			int key = 10;
			if (points.size() > 100) {
				key = 20;
				if (points.size() > 500) {
					key = 50;
				}
			}
			for (int i = 0; i < points.size(); i++) {
				linkPoints.add(points.get(i));
				linkTraces.add(traces.get(i));
				if (i % key == 0 && i > 0) {// 每key个点选取一个作为关键点，显示定位时间
					keyPoints.add(traces.get(i));
				}
			}
			for (int i = 0; i < traces.size(); i++) {// 提取出轨迹点的时间 单位 毫秒
				arrayMillSeconds.add(praseStrToMillsecond(traces.get(i).getcreateTime()));

			}
			selectid.clear();
			if (points.size() >= 3) {
				for (int i = 0; i < points.size() - 2; i++) {
					float d12 = AMapUtils.calculateLineDistance(points.get(i), points.get(i + 1));
					float d23 = AMapUtils.calculateLineDistance(points.get(i + 1), points.get(i + 2));
					if (d12 > 10 * d23 && d12 > 50.0) {
						selectid.add(i);

					} else if (i == (points.size() - 3) && d12 * 10 < d23 && d23 > 50.0) {
						selectid.add(i + 1);

					}
				}
			}
		} else {
			for (int i = 0; i < points.size(); i++) {
				linkPoints.add(points.get(i));
				linkTraces.add(traces.get(i));

			}
			for (int i = 0; i < traces.size(); i++) {// 提取出轨迹点的时间 单位 毫秒
				arrayMillSeconds.add(praseStrToMillsecond(traces.get(i).getcreateTime()));

			}
		}
		Log.i("trailadapter", "过滤后坐标数：" + points.size() + "步行规划路段数：" + selectid.size());
	}

	// 高德画路径
	public void AMap_drawpath_optimize(List<LatLng> points) {

		// aMap.clear();
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16));
		// 轨迹分段讨论
		if (selectid.size() == 0) {
			PolylineOptions options;
			options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
			options.addAll(points);
			if (polyline != null) {
				polyline.remove();
			}
			polyline = aMap.addPolyline(options);
			// BDMap_drawpath_normal(points);
		} else if (selectid.size() == 1) {
			if (selectid.get(0) == 0) {
				PolylineOptions options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				for (int i = 1; i < points.size(); i++) {
					options.add(points.get(i));

				}
				aMap.addPolyline(options);
			} else if (selectid.get(0) == points.size() - 2) {
				PolylineOptions options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				for (int i = 0; i < points.size() - 1; i++) {
					options.add(points.get(i));

				}
				aMap.addPolyline(options);
			} else {
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				for (int i = 0; i <= selectid.get(0); i++) {
					options.add(points.get(i));

				}
				aMap.addPolyline(options);
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				for (int i = selectid.get(0) + 1; i < points.size(); i++) {
					options.add(points.get(i));

				}
				aMap.addPolyline(options);
			}
		} else if (selectid.size() > 1) {
			if (selectid.get(0) == 0) {
				PolylineOptions options;

				for (int i = 1; i < selectid.size(); i++) {
					options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);
					for (int j = selectid.get(i - 1) + 1; j <= selectid.get(i); j++) {
						options.add(points.get(j));

					}
					aMap.addPolyline(options);
				}

			} else {
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);
				for (int j = 0; j <= selectid.get(0); j++) {
					options.add(points.get(j));

				}
				aMap.addPolyline(options);

				for (int i = 1; i < selectid.size(); i++) {
					options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
					for (int j = selectid.get(i - 1) + 1; j <= selectid.get(i); j++) {
						options.add(points.get(j));

					}
					aMap.addPolyline(options);
				}
			}
			if (selectid.get(selectid.size() - 1) == points.size() - 2) {

			} else {
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);
				for (int j = selectid.get(selectid.size() - 1) + 1; j < points.size(); j++) {
					options.add(points.get(j));

				}
				aMap.addPolyline(options);
			}
		}

		if (selectid.size() > 0) {// 存在需要进行路径规划的轨迹
			showDialog(getResources().getString(R.string.tips_dlgtle_init),
					getResources().getString(R.string.tips_dlgmsg_optmizetrace));

			s_id = 0;
			if (trailobj.getSportType() <= 3 || trailobj.getSportType() == 6) {// 步行、骑行、轮滑、其他状态采用步行规划
				// for(int i=0;i<selectid.size();i++){//不能循环进行路径规划，循环下异步执行轨迹会乱
				// 具体原因不详

				Log.i("trailadapter", "步行规划：" + selectid.get(0) + "->" + (selectid.get(0) + 1));
				final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
						AMapUtil.convertToLatLonPoint(points.get(selectid.get(0))),
						AMapUtil.convertToLatLonPoint(points.get(selectid.get(0) + 1)));
				// RouteSearch.WalkDefault
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
				routeSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
				// }
			} else if (trailobj.getSportType() == 4) {// 驾车模式采用驾车规划
				// for(int i=0;i<selectid.size();i++){

				Log.i("trailadapter", "驾车规划：" + selectid.get(0) + "->" + (selectid.get(0) + 1));
				final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
						AMapUtil.convertToLatLonPoint(points.get(selectid.get(0))),
						AMapUtil.convertToLatLonPoint(points.get(selectid.get(0) + 1)));
				//// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
				DriveRouteQuery query = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
				routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划步行模式查询
				// }
			}
		}

		// for(int i=0;i<NetPoints.size();i++){
		// circle = aMap.addCircle(new CircleOptions().center(NetPoints.get(i))
		// .radius(5).strokeColor(Color.BLUE)
		// .fillColor(Color.argb(0, 255, 255, 255)).strokeWidth(20));
		// }
		// Log.i("trailadapter","过滤后剩下点的个数："+points.size()+"");
		/*
		 * for(int i=0;i<points.size();i++){ Log.i("trailadapter",
		 * "过滤，第"+i+"个记录：("+points.get(i).longitude+","+points.get(i).latitude+
		 * ")");
		 * 
		 * }
		 */
		aMap.addMarker(
				new MarkerOptions().position(points.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
						.title(traces.get(0).getcreateTime()).anchor(0.5f, 0.5f));
		aMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
				.title(traces.get(points.size() - 1).getcreateTime()).anchor(0.5f, 0.5f));
//		for (int i = 0; i < keyPoints.size(); i++) {
//			aMap.addMarker(new MarkerOptions()
//					.position(new LatLng(keyPoints.get(i).getLatitude(), keyPoints.get(i).getLongitude()))
//					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_keypoint))
//					.title(keyPoints.get(i).getcreateTime()).anchor(0.5f, 0.5f));
//
//		}
		// 设置所有maker显示在当前可视区域地图中
		// include(points.get(0)).include(points.get(points.size()-1)).build()
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < points.size(); i++) {
			builder.include(points.get(i));
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
	}

	public void AMap_drawpath_normal(List<LatLng> points) {

		// aMap.clear();
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 16));

		PolylineOptions options;
		options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性

		options.addAll(points);
		if (polyline != null) {
			polyline.remove();
		}
		polyline = aMap.addPolyline(options);
		aMap.addMarker(
				new MarkerOptions().position(points.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.start))
						.title(traces.get(0).getcreateTime()).anchor(0.5f, 0.5f));
		aMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.end))
				.title(traces.get(points.size() - 1).getcreateTime()).anchor(0.5f, 0.5f));
//		for (int i = 0; i < keyPoints.size(); i++) {
//			aMap.addMarker(new MarkerOptions()
//					.position(new LatLng(keyPoints.get(i).getLatitude(), keyPoints.get(i).getLongitude()))
//					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_keypoint))
//					.title(keyPoints.get(i).getcreateTime()).anchor(0.5f, 0.5f));
//
//		}
		// 设置所有maker显示在当前可视区域地图中
		// include(points.get(0)).include(points.get(points.size()-1)).build()
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (int i = 0; i < points.size(); i++) {
			builder.include(points.get(i));
		}
		aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
	}

	private long praseStrToMillsecond(String time) {
		long duration = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try { // 计算时间
			Date d1 = df.parse(time);
			duration = d1.getTime();
		} catch (Exception e) {

		}
		return duration;
	}

	private int praseTimeToPosition(long currentMills) {
		int position = 0;

		for (int i = 0; i < arrayMillSeconds.size(); i++) {
			long mills = arrayMillSeconds.get(i);

			if (mills >= currentMills && mills > 0) {
				position = i;
				break;
			}
		}
		// Log.i("mark","progress = "+progress+ ",current pos = "+position);
		return position;
	}

	private String praseProgressToStr(int progress) {
		long duration = 0;
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		try { // 计算时间差

			duration = startMills + progress * 1000;

			Date d2 = new Date(duration);
			return df.format(d2);
		} catch (Exception e) {
			return "error";
		}

	}

	private int praseProgressToPosition(int progress) {
		int position = 0;
		long currentMills = startMills + (progress) * 1000;

		for (int i = 0; i < arrayMillSeconds.size(); i++) {
			long mills = arrayMillSeconds.get(i);

			if (mills >= currentMills && mills > 0) {
				position = i;
				break;
			}
		}
		// Log.i("mark","progress = "+progress+ ",current pos = "+position);
		return position;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, final Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == getActivity().RESULT_OK) {
			switch (requestCode) {
			// 如果评论Activity返回的结果，根据该结果进行图片上传
			case REQUESTMARK:
				initMarker();
				Log.i("mark", "upmark");
				// 如果有wifi网络，直接开始上传
				if (Common.checkNetworkState(context) == ConnectivityManager.TYPE_WIFI) {
					Toast.makeText(context, R.string.tips_uploadinbgbegin, Toast.LENGTH_SHORT).show();
					String commentTime = data.getStringExtra("createTime");
					Intent intent = new Intent(context, CommentUploadService.class);
					intent.putExtra("createTime", commentTime);
					context.startService(intent);
					// commentUploadService.uploadComment(Common.getUserId(getApplicationContext()),
					// commentTime);
				} // 如果是数据流量连接，第一次使用询问是否上传
				else if (Common.checkNetworkState(context) == ConnectivityManager.TYPE_MOBILE
						&& !Common.isOnlyWifiUploadPic(context)) {
					SharedPreferences sp = getActivity().getSharedPreferences("config",
							android.content.Context.MODE_PRIVATE);
					if (sp.getInt(LoginActivity.mobConnectFirst, 0) == 0) {
						Editor edit = sp.edit();
						edit.putInt(LoginActivity.mobConnectFirst, 1);
						edit.commit();
						CustomDialog.Builder builder = new CustomDialog.Builder(context);
						builder.setMessage(getResources().getString(R.string.mobconnect_tips));
						builder.setTitle(getResources().getString(R.string.tip));
						builder.setPositiveButton(getResources().getString(R.string.confirm),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String commentTime = data.getStringExtra("createTime");
										Intent intent = new Intent(context, CommentUploadService.class);
										intent.putExtra("createTime", commentTime);
										context.startService(intent);
										// commentUploadService.uploadComment(
										// Common.getUserId(getApplicationContext()),
										// commentTime);
										dialog.dismiss();
									}
								});
						builder.setNegativeButton(getResources().getString(R.string.cancl),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String commentTime = data.getStringExtra("createTime");
										String userId = Common.getUserId(context);
										SharedPreferences.Editor editor = uploadCache.edit();
										editor.putString(commentTime, userId);
										editor.commit();
										dialog.dismiss();
									}
								});
						builder.create().show();
					} else {
						String commentTime = data.getStringExtra("createTime");
						Intent intent = new Intent(context, CommentUploadService.class);
						intent.putExtra("createTime", commentTime);
						context.startService(intent);
						// commentUploadService.uploadComment(
						// Common.getUserId(getApplicationContext()),
						// commentTime);
					}
				} else {
					// 如果没有网络，将上传相关信息写到cache文件
					Toast.makeText(context, getResources().getString(R.string.tips_uploadpic_nonet), Toast.LENGTH_SHORT)
							.show();
					String commentTime = data.getStringExtra("createTime");
					String userId = Common.getUserId(context);
					SharedPreferences.Editor editor = uploadCache.edit();
					editor.putString(commentTime, userId);
					editor.commit();
				}
				break;
			}
		}
		//用于qq空间分享
		mIUilistener = new MyIUilistener();
		Tencent.onActivityResultData(requestCode, resultCode, data, mIUilistener);
		if(requestCode == Constants.REQUEST_API){
			if(requestCode == Constants.REQUEST_QQ_SHARE || 
				requestCode == Constants.REQUEST_QZONE_SHARE || requestCode == Constants.REQUEST_OLD_SHARE){
				Tencent.handleResultData(data, mIUilistener);
			}
		}
	}

	private class OnSeekBarChangeListenerImp implements SeekBar.OnSeekBarChangeListener {

		// 触发操作，拖动
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			currentProgress = progress;
			moveText.layout((int) (progress * moveStep), 20, screenWidth, 80);
			moveText.setText(praseProgressToStr(progress));
			MarkerOptions options1 = new MarkerOptions();

			if (progress >= 0) {
				options1.position(points.get(praseProgressToPosition(progress)))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark));
				if (selectedMarker != null) {
					selectedMarker.remove();
					selectedMarker = null;
				}
				selectedMarker = aMap.addMarker(options1);

			}

		}

		// 表示进度条刚开始拖动，开始拖动时候触发的操作
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		// 停止拖动时候
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * 7/27起 停用该方法
	 * 
	 * public void addPoi(){ Log.i("mark", "latlng size = "+linkPoints.size()+"
	 * gpsdata size = "+ linkTraces.size()); List<GpsData> arrayTrace = new
	 * ArrayList<GpsData>(linkTraces); List<LatLng> arrayLatLng = new
	 * ArrayList<LatLng>(linkPoints); String jsonTrace
	 * =GsonHelper.toJson(arrayTrace); String jsonLatLng
	 * =GsonHelper.toJson(arrayLatLng); String jsonMarkLatLng =
	 * GsonHelper.toJson(MarkPoints); String jsonMillsTime =
	 * GsonHelper.toJson(arrayMillSeconds); Intent intent = new Intent();
	 * intent.setClass(context, MarkingActivity.class);
	 * intent.putExtra("jsonTrace", jsonTrace); intent.putExtra("jsonLatLng",
	 * jsonLatLng); intent.putExtra("jsonMarkLatLng", jsonMarkLatLng);
	 * intent.putExtra("jsonMillsTime", jsonMillsTime); startActivity(intent);
	 * }*
	 */
	public void deleteTrace() {
		if (isOnline) {// 删云端
			showDialog(getResources().getString(R.string.tip), getResources().getString(R.string.tips_deletedlgmsg));

			ArrayList<Long> tobedeleteNo = new ArrayList<Long>();
			tobedeleteNo.add(trailobj.getTraceNo());
			String tobedelete = GsonHelper.toJson(tobedeleteNo);
			// Log.i("trailadapter","删除:"+tobedelete);
			PostDeleteTrail deletetrail = new PostDeleteTrail(handler, URL_GETTRAIL, Common.getUserId(context),
					tobedelete, Common.getDeviceId(context));
			deletetrail.start();
		}
		// 删本地
		helper.deleteTrailByTraceNo(trailobj.getTraceNo(), Common.getUserId(context));
		if (!isOnline) {// 只在本地有这条轨迹，那么删完本地就发广播，否则，在删云端的handler中发广播
			Intent intent = new Intent();
			intent.setAction(REFRESH_ACTION);
			context.sendBroadcast(intent);
			getActivity().finish();
		}
		// 同时删除兴趣点
		myComment.deleteComment(trailobj.getStartTime(), trailobj.getEndTime());
	}

	public void shareToWX() {
		if (Common.url_wx == null || Common.url_wx.equals("")) {
			Common.url_wx = getResources().getString(R.string.url_wx);
		}
		String title = getResources().getString(R.string.share_title);
		String detail = getResources().getString(R.string.distancelable) + ":"
				+ Common.transformDistance(trailobj.getDistance()) + getResources().getString(R.string.disunit) + "\n"
				+ getResources().getString(R.string.durationlable) + ":" + Common.transformTime(trailobj.getDuration());
//		ShareToWeChat.shareWeb((context),
//				Common.url_wx + "uid=" + Common.getUserId(context) + "&tid=" + trailobj.getTraceNo(), isTimeLine, title,
//				detail);
		ShareToWeChat.shareWeb((context),
				"http://219.218.118.176:8089/Share/PoMobile.ashx?" + "uid=" + Common.getUserId(context) + "&tid=" + trailobj.getTraceNo(), isTimeLine, title,
				detail);

	}

	public void uploadBeforeShare(boolean isTimeLine) {
		this.isTimeLine = isTimeLine;
		if (isOnline) {// 在线的轨迹，云端已有，不用上传
			shareToWX();
			return;
		}
		List<StepData> steps_upload = new ArrayList<StepData>();

		if (trailobj.getSportType() == 1) {
			steps_upload.add(stepdata);
		}
		List<TraceData> traces_upload = new ArrayList<TraceData>();

		traces_upload.add(trailobj);
		PostGpsData gpsthread = new PostGpsData(shareHandler, URL_GPSDATA, GsonHelper.toJson(traces),
				Common.getDeviceId(context));
		gpsthread.start();
		String traceinfo = GsonHelper.toJson(traces_upload);
		String stepinfo = "";
		if (steps_upload.size() > 0) {
			stepinfo = GsonHelper.toJson(steps_upload);
		}
		// Log.i("trailadapter","上传的轨迹："+traceinfo+","+stepinfo);
		PostEndTrail endTrailThread = new PostEndTrail(shareHandler, URL_ENDTRAIL, traceinfo, stepinfo,
				Common.getDeviceId(context));
		endTrailThread.start();
	}

	private Handler shareHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				canShare1 = true;
				if (canShare1 && canShare2) {
					shareToWX();
				}
				break;
			case 1:
				ToastUtil.show(context, getResources().getString(R.string.tips_share_fail));

				break;
			case 2:

				canShare2 = true;
				if (canShare1 && canShare2) {
					shareToWX();
				}
				break;
			case 3:
				ToastUtil.show(context, getResources().getString(R.string.tips_share_fail));

				break;
			case 10:
				ToastUtil.show(context, getResources().getString(R.string.tips_share_nonet2));
				break;
			case 11:
				break;
			}
		}
	};

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		// setmapvisibility();
		super.onResume();
		mapView.onResume();
		if (traces != null && traces.size() > 0) {
			Log.i("mark", "重新绘制marker");
			initMarker();// 重新绘制marker
		}
		// bdMapView.onResume();
		// setMapVisibility(maptype);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		// mapView.setVisibility(View.GONE);加上这两句会黑屏
		// bdMapView.setVisibility(View.GONE);
		super.onPause();
		mapView.onPause();
		// bdMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		myComment.stopModel();
		myComment = null;
		// bdMapView.onDestroy();
	}

	/**
	 * 显示进度条对话框
	 */
	public void showDialog(String title, String message) {
		if (proDialog == null)
			proDialog = new ProgressDialog(context);
		proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		proDialog.setIndeterminate(false);
		proDialog.setCancelable(true);
		proDialog.setTitle(title);
		proDialog.setMessage(message);
		proDialog.show();
	}

	/**
	 * 隐藏进度条对话框
	 */
	public void dismissDialog() {
		if (proDialog != null) {
			proDialog.dismiss();
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 1000) {
			if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
				driveRouteResult = result;
				List<DriveStep> driveSteps = driveRouteResult.getPaths().get(0).getSteps();
				List<LatLonPoint> drivePoints = new ArrayList<LatLonPoint>();
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				options.setDottedLine(true);
				options.add(points.get(selectid.get(s_id)));

				for (int i = 0; i < driveSteps.size(); i++) {
					drivePoints = driveSteps.get(i).getPolyline();

					for (int j = 0; j < drivePoints.size(); j++) {

						options.add(new LatLng(drivePoints.get(j).getLatitude(), drivePoints.get(j).getLongitude()));

					}
				}
				options.add(points.get(selectid.get(s_id) + 1));
				aMap.addPolyline(options);

				Log.i("trailadapter", "addPloyline==>" + selectid.get(s_id) + "-line-" + (selectid.get(s_id) + 1));
				// 插入Linkedlist
				if (s_id == 0) {
					int startpos = selectid.get(s_id);
					int insertpos = startpos + 1;
					for (int i = 1; i < options.getPoints().size() - 1; i++) {
						linkPoints.add(insertpos, options.getPoints().get(i));
						linkTraces.add(insertpos, new GpsData(linkTraces.get(startpos).getuserId(),
								linkTraces.get(startpos).getcreateTime(), options.getPoints().get(i).longitude,
								options.getPoints().get(i).latitude, 0, 0, linkTraces.get(startpos).gettraceNo()));
						insertpos++;
					}
					start.add(startpos);
					end.add(insertpos);
					Log.i("trailadapter",
							"start:" + startpos + "  end:" + insertpos + "  length:" + options.getPoints().size());
				} else {
					int deltpos = 0;
					for (int i = 0; i < start.size(); i++) {
						deltpos += end.get(i) - start.get(i);
					}
					int startpos = selectid.get(s_id) + deltpos;
					int insertpos = startpos + 1;
					for (int i = 1; i < options.getPoints().size() - 1; i++) {
						linkPoints.add(insertpos, options.getPoints().get(i));
						linkTraces.add(insertpos, new GpsData(linkTraces.get(startpos).getuserId(),
								linkTraces.get(startpos).getcreateTime(), options.getPoints().get(i).longitude,
								options.getPoints().get(i).latitude, 0, 0, linkTraces.get(startpos).gettraceNo()));
						insertpos++;
					}
					start.add(startpos);
					end.add(insertpos);
					Log.i("trailadapter",
							"start:" + startpos + "  end:" + insertpos + "  length:" + options.getPoints().size());
				}
				s_id++;
				if (s_id < selectid.size()) {
					Log.i("trailadapter", "驾车规划：" + selectid.get(s_id) + "->" + (selectid.get(s_id) + 1));
					final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
							AMapUtil.convertToLatLonPoint(points.get(selectid.get(s_id))),
							AMapUtil.convertToLatLonPoint(points.get(selectid.get(s_id) + 1)));
					DriveRouteQuery query = new DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
					routeSearch.calculateDriveRouteAsyn(query);// 异步路径规划步行模式查询
				} else {
					Log.i("trailadapter", "驾车规划完成");
					dismissDialog();
					// BDMap_drawpath_optimize(new ArrayList<LatLng>(linkPoints)
					// , start, end);
				}
			} else {
				dismissDialog();
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				options.setDottedLine(true);
				options.add(points.get(selectid.get(s_id)));
				options.add(points.get(selectid.get(s_id) + 1));
				aMap.addPolyline(options);

				Log.i("trailadapter", "noresult");
			}
		} else {
			dismissDialog();
			PolylineOptions options;
			options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
			options.setDottedLine(true);
			options.add(points.get(selectid.get(s_id)));
			options.add(points.get(selectid.get(s_id) + 1));
			aMap.addPolyline(options);

			Log.i("trailadapter", "error" + rCode);
		}
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 1000) {
			if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
				walkRouteResult = result;
				List<WalkStep> walkSteps = walkRouteResult.getPaths().get(0).getSteps();
				List<LatLonPoint> walkPoints = new ArrayList<LatLonPoint>();// 提取出步行规划的坐标
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				options.setDottedLine(true);
				options.add(points.get(selectid.get(s_id)));

				for (int i = 0; i < walkSteps.size(); i++) {
					walkPoints = walkSteps.get(i).getPolyline();

					for (int j = 0; j < walkPoints.size(); j++) {

						options.add(new LatLng(walkPoints.get(j).getLatitude(), walkPoints.get(j).getLongitude()));

					}

				}
				options.add(points.get(selectid.get(s_id) + 1));
				aMap.addPolyline(options);
				Log.i("trailadapter", "addPloyline==>" + selectid.get(s_id) + "-line-" + (selectid.get(s_id) + 1));
				// 插入Linkedlist
				if (s_id == 0) {
					int startpos = selectid.get(s_id);
					int insertpos = startpos + 1;
					for (int i = 1; i < options.getPoints().size() - 1; i++) {
						linkPoints.add(insertpos, options.getPoints().get(i));
						linkTraces.add(insertpos, new GpsData(linkTraces.get(startpos).getuserId(),
								linkTraces.get(startpos).getcreateTime(), options.getPoints().get(i).longitude,
								options.getPoints().get(i).latitude, 0, 0, linkTraces.get(startpos).gettraceNo()));
						insertpos++;
					}
					start.add(startpos);
					end.add(insertpos);
					Log.i("trailadapter",
							"start:" + startpos + "  end:" + insertpos + "  length:" + options.getPoints().size());
				} else {
					int deltpos = 0;
					for (int i = 0; i < start.size(); i++) {
						deltpos += end.get(i) - start.get(i);
					}
					int startpos = selectid.get(s_id) + deltpos;
					int insertpos = startpos + 1;
					for (int i = 1; i < options.getPoints().size() - 1; i++) {
						linkPoints.add(insertpos, options.getPoints().get(i));
						linkTraces.add(insertpos, new GpsData(linkTraces.get(startpos).getuserId(),
								linkTraces.get(startpos).getcreateTime(), options.getPoints().get(i).longitude,
								options.getPoints().get(i).latitude, 0, 0, linkTraces.get(startpos).gettraceNo()));
						insertpos++;
					}
					start.add(startpos);
					end.add(insertpos);
					Log.i("trailadapter",
							"start:" + startpos + "  end:" + insertpos + "  length:" + options.getPoints().size());
				}
				s_id++;
				if (s_id < selectid.size()) {
					Log.i("trailadapter", "步行规划：" + selectid.get(s_id) + "->" + (selectid.get(s_id) + 1));
					final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
							AMapUtil.convertToLatLonPoint(points.get(selectid.get(s_id))),
							AMapUtil.convertToLatLonPoint(points.get(selectid.get(s_id) + 1)));
					// RouteSearch.WalkDefault
					WalkRouteQuery query = new WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
					routeSearch.calculateWalkRouteAsyn(query);
				} else {
					Log.i("trailadapter", "步行规划完成");
					dismissDialog();
					// BDMap_drawpath_optimize(new
					// ArrayList<LatLng>(linkPoints),start,end);
				}
			} else {
				dismissDialog();
				PolylineOptions options;
				options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
				options.setDottedLine(true);
				options.add(points.get(selectid.get(s_id)));
				options.add(points.get(selectid.get(s_id) + 1));
				aMap.addPolyline(options);

				Log.i("trailadapter", "noresult");
			}
		} else {
			dismissDialog();
			PolylineOptions options;
			options = new PolylineOptions().width(10).geodesic(true).color(Color.BLUE);// 初始化轨迹属性
			options.setDottedLine(true);
			options.add(points.get(selectid.get(s_id)));
			options.add(points.get(selectid.get(s_id) + 1));
			aMap.addPolyline(options);

			Log.i("trailadapter", "error" + rCode);
		}
	}

	@Override
	public void onMapLoaded() {
		// TODO Auto-generated method stub
		initPathData();
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		boolean isUserMarker = false;
		int pos = 0;
		for (int i = 0; i < arrayMarker.size(); i++) {
			if (marker.equals(arrayMarker.get(i))) {
				pos = i;
				isUserMarker = true;
				break;
			}
		}
		if (isUserMarker) {
			Log.i("mark", "点的是标注点");
			View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.markinfowindow, null);
			// Log.i("LogDemo", "getInfoWindow");
			render_mark(marker, pos, infoWindow);
			return infoWindow;
		} else {
			View infoWindow = getActivity().getLayoutInflater().inflate(R.layout.traceinfowindow, null);
			// Log.i("LogDemo", "getInfoWindow");
			render_keypoint(marker, infoWindow);
			return infoWindow;
		}

	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		for (int i = 0; i < arrayMarker.size(); i++) {
			if (arrayMarker.get(i).isInfoWindowShown()) {
				Log.i("mark", "hideinfowindow");
				arrayMarker.get(i).hideInfoWindow();// 隐藏infowindow
			}
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		Log.i("mark", "markerclick");

		return false;
	}

	/**
	 * 自定义infowinfow窗口
	 */
	public void render_mark(final Marker marker, int pos, View view) {
		ImageButton close = (ImageButton) view.findViewById(R.id.infowindow_close);

		SlideListView markView = (SlideListView) view.findViewById(R.id.listViewMark);
		markView.initSlideMode(SlideListView.MOD_LEFT);
		ArrayList<HashMap<String, Object>> singleItem = new ArrayList<HashMap<String, Object>>();
		singleItem.add(items.get(pos));
		ListBaseAdapter3 listAdapter3 = new ListBaseAdapter3(context, myComment, singleItem, "mark", pos);
		markView.setAdapter(listAdapter3);
		listAdapter3.setDeleCommListener(new DeleCommListener() {
			
			@Override
			public void clickDelete(String dateTime, int position) {
				// TODO Auto-generated method stub
				deleteEvent(dateTime, position);
			}
		});
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				marker.hideInfoWindow();
			}

		});
	}

	/**
	 * 自定义infowinfow窗口
	 */
	public void render_keypoint(final Marker marker, View view) {
		ImageButton close = (ImageButton) view.findViewById(R.id.infowin_close);

		String title = marker.getTitle();
		TextView titleUi = ((TextView) view.findViewById(R.id.infowin_time));
		if (title != null) {
			SpannableString titleText = new SpannableString(title);
			titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, titleText.length(), 0);
			titleUi.setTextSize(15);
			titleUi.setText(titleText);
			// Log.i("LogDemo", "title"+title);
		} else {
			titleUi.setText("");
		}

		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				marker.hideInfoWindow();
			}

		});
	}

	private void deleteEvent(final String dateTime, final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(context);
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(getResources().getString(R.string.tips_deletedlgmsg_album));
		builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				deleteComment(dateTime, position);
			}
		});
		builder.setNegativeButton(getResources().getString(R.string.cancl), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();

	}

	private void deleteComment(String dateTime, int listPosition) {
		myComment.deleteComment(dateTime, listPosition);
	}

	private void initPopupWindow() {
		View contentView = LayoutInflater.from(context).inflate(R.layout.path_info, null);
		mPopupWindow = new PopupWindow(contentView);
		//获取屏幕宽度
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mPopupWindow.setWidth(width);
		mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);

		distance = (TextView) contentView.findViewById(R.id.distance_info);
		duration = (TextView) contentView.findViewById(R.id.during_info);
		time = (TextView) contentView.findViewById(R.id.time_info);
		speed = (TextView) contentView.findViewById(R.id.speed_info);
		step = (TextView) contentView.findViewById(R.id.step_info);
		calorie = (TextView) contentView.findViewById(R.id.calorie_info);

		distance.setText(Common.transformDistance(trailobj.getDistance()));
		duration.setText(Common.transformTime(trailobj.getDuration()));
		time.setText(trailobj.getStartTime());
		speed.setText(Common.transformSpeed(trailobj.getDistance() / trailobj.getDuration()) + " "
				+ getResources().getString(R.string.speedunit));
		step.setText(stepstr);
		calorie.setText(trailobj.getCalorie() + " " + getResources().getString(R.string.calorieunit));

		mPopupWindow.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mPopupWindow.dismiss();
					return true;
				}
				return false;
			}
		});
	}
	private void initPOI(){
		//从服务器下载停留时长、行为类型、同伴人数、关系等选项的数据
		PostPointOfInterestData pointOfInterest = new PostPointOfInterestData(handler1, URL_GETPOI);
		pointOfInterest.start();
	}
	private void initPOIEN(){
		//英文版
		PostPointOfInterestDataEn pointOfInterestEn = new PostPointOfInterestDataEn(handler1, URL_GETPOI);
		pointOfInterestEn.start();
	}
	private class MyIUilistener implements IUiListener{

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(UiError arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public void ShareToQQ(){
		String title = getResources().getString(R.string.share_title);
		String detail = getResources().getString(R.string.distancelable) + ":"
				+ Common.transformDistance(trailobj.getDistance()) + getResources().getString(R.string.disunit) + "\n"
				+ getResources().getString(R.string.durationlable) + ":" + Common.transformTime(trailobj.getDuration());
		params = new Bundle();
		params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
		params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
		params.putString(QQShare.SHARE_TO_QQ_SUMMARY, detail);
		params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, "http://219.218.118.176:8089/Share/PoMobile.ashx?" + 
							"uid=" + Common.getUserId(context) + "&tid=" + trailobj.getTraceNo());
		ArrayList<String> imgUrlList = new ArrayList<String>();
		imgUrlList.add("http://footprint.lisoft.com.cn/images/logo.png");
		params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imgUrlList);//图片地址
		ThreadManager.getMainHandler().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTencent.shareToQQ(getActivity(), params, mIUilistener);
			}
		});
	}
	//用于qq空间分享
	//此功能有缺陷，不知道怎么将工程中的res/drawable下的图片添加到分享中，待改进
	public void ShareToQZone(){
		String title = getResources().getString(R.string.share_title);
		String detail = getResources().getString(R.string.distancelable) + ":"
				+ Common.transformDistance(trailobj.getDistance()) + getResources().getString(R.string.disunit) + "\n"
				+ getResources().getString(R.string.durationlable) + ":" + Common.transformTime(trailobj.getDuration());
		params = new Bundle();
		params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
		params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
		params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, detail);
		params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, "http://219.218.118.176:8089/Share/PoMobile.ashx?" + 
							"uid=" + Common.getUserId(context) + "&tid=" + trailobj.getTraceNo());
		ArrayList<String> imgUrlList = new ArrayList<String>();
//		Resources r = context.getApplicationContext().getResources();
//		Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
//		+r.getResourcePackageName(R.drawable.ic_launcher_wx)+"/"
//		+r.getResourceTypeName(R.drawable.ic_launcher_wx)+"/"
//		+r.getResourceEntryName(R.drawable.ic_launcher_wx));
//		String a = uri.toString();
		imgUrlList.add("http://footprint.lisoft.com.cn/images/logo.png");
		params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgUrlList);//图片地址
		ThreadManager.getMainHandler().post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTencent.shareToQzone(getActivity(), params, mIUilistener);
			}
		});
	}
	public String getAbsoluteImagePath(Context context, Uri uri){
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor =  context.getContentResolver().query(uri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	public static String getRealFilePath( final Context context, final Uri uri ) {  
	    if ( null == uri ) return null;  
	    final String scheme = uri.getScheme();  
	    String data = null;  
	    if ( scheme == null )  
	        data = uri.getPath();  
	    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {  
	        data = uri.getPath();  
	    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {  
	        Cursor cursor = context.getContentResolver().query( uri, new String[] { ImageColumns.DATA }, null, null, null );  
	        if ( null != cursor ) {  
	            if ( cursor.moveToFirst() ) {  
	                int index = cursor.getColumnIndex( ImageColumns.DATA );  
	                if ( index > -1 ) {  
	                    data = cursor.getString( index );  
	                }  
	            }  
	            cursor.close();  
	        }  
	    }  
	    return data;  
	}

	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
