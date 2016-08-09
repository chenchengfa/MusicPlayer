package com.example.musicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GuideActivity extends Activity {
	private ArrayList<View> viewList = new ArrayList<View>();
	private ViewPager viewPager;
	private MyPagerAdapter adapter;
	private int[] resId = { R.drawable.guide_bg_0, R.drawable.guide_bg_1,
			R.drawable.guide_bg_2, R.drawable.guide_bg_3, R.drawable.guide_bg_4 };
	private Button mStartBtn;
	private ImageView[] mDots;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);
		viewPager = (ViewPager) findViewById(R.id.viewPager1);
		mStartBtn = (Button) findViewById(R.id.startBtn);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		initViewPager();
		initDots();
		adapter = new MyPagerAdapter();
		viewPager.setAdapter(adapter);

	}

	// 初始化viewPager
	private void initViewPager() {
		// 循环resId数组，
		for (int i = 0; i < resId.length; i++) {
			// 填充Viewpager的布局
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.guide_layout, null);
			ImageView image = (ImageView) view.findViewById(R.id.imageView1);
			image.setImageResource(resId[i]);
			// 每个页面保存到集合中
			viewList.add(view);
		}
	}
	// 初始化底部小点
	private void initDots() {
	 	RelativeLayout dotsLayout = (RelativeLayout) findViewById(R.id.dotsGroup);
		mDots = new ImageView[resId.length];
		// 循环取得小点图片
		for (int i = 0; i < resId.length; i++) {
			//获得布局中的子类
			mDots[i] = (ImageView) dotsLayout.getChildAt(i);
				// 都设为灰色
			//	mDots[i].setImageResource(R.drawable.page_indicator);
			}
		// 默认第1个为选中状态
			mDots[0].setImageResource(R.drawable.page_indicator_focused);
		}
	 

	// PagerAdapter
	class MyPagerAdapter extends PagerAdapter {
		// 返回要滑动的View的个数
		@Override
		public int getCount() {
			return viewList.size();
		}

		// 判断从instantiateItem()返回来的Key与当前的View是否
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		// 从当前container中删除指定位置（position）的View
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
			((ViewPager) container).removeView(viewList.get(position));
		}

		// instantiateItem()：做了两件事，第一：将当前视图添加到container中，第二：返回当前View
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(viewList.get(position));
			return viewList.get(position);
		}
	}

	//
	class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
	 
		public void onPageScrollStateChanged(int position) {
		}

		//
		@Override
		public void onPageScrolled(int position, float arg1, int arg2) {

		}

		// 当选中页面时
		@Override
		public void onPageSelected(int selected) {
			// 选中最后一页时，显示按钮
			showButton(selected);
			//选中的页面显示小圆点获得焦点
		 	for (int i = 0; i < mDots.length; i++) {
		 	mDots[selected].setImageResource(R.drawable.page_indicator_focused);
				//没有选中的页面
		 		if (selected != i) {
		 		mDots[i].setImageResource(R.drawable.page_indicator);
				}
		 	}
		}
	}
		
		
	 

	// 选择最后一页时，显示按钮
	private void showButton(int selected) {
		// 当为最后一页时,才显示按钮,button按钮是放在activity_guide布局中
		if (selected == (resId.length - 1)) {
			mStartBtn.setVisibility(View.VISIBLE);
			// 设置按钮点击监听，处理点击事件
			mStartBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// SharedPreferences数据保存
					setGuideSP();
				}
			});
		} else {
			// viewPager页面往回拉时，也要不可见
			mStartBtn.setVisibility(View.INVISIBLE);
		}
	}

	// 点击button,SP数据保存
	private void setGuideSP() {
		// 保存isFirst布尔值到guide中，用于判断
		SharedPreferences sp = getSharedPreferences("guide", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("isFirst", false);
		editor.commit();
		// 跳转到主界面
		Intent intent = new Intent(GuideActivity.this, MainActivity.class);
		startActivity(intent);
		// 销毁本界面
		finish();
	}

}
