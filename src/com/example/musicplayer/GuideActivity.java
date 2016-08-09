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

	// ��ʼ��viewPager
	private void initViewPager() {
		// ѭ��resId���飬
		for (int i = 0; i < resId.length; i++) {
			// ���Viewpager�Ĳ���
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.guide_layout, null);
			ImageView image = (ImageView) view.findViewById(R.id.imageView1);
			image.setImageResource(resId[i]);
			// ÿ��ҳ�汣�浽������
			viewList.add(view);
		}
	}
	// ��ʼ���ײ�С��
	private void initDots() {
	 	RelativeLayout dotsLayout = (RelativeLayout) findViewById(R.id.dotsGroup);
		mDots = new ImageView[resId.length];
		// ѭ��ȡ��С��ͼƬ
		for (int i = 0; i < resId.length; i++) {
			//��ò����е�����
			mDots[i] = (ImageView) dotsLayout.getChildAt(i);
				// ����Ϊ��ɫ
			//	mDots[i].setImageResource(R.drawable.page_indicator);
			}
		// Ĭ�ϵ�1��Ϊѡ��״̬
			mDots[0].setImageResource(R.drawable.page_indicator_focused);
		}
	 

	// PagerAdapter
	class MyPagerAdapter extends PagerAdapter {
		// ����Ҫ������View�ĸ���
		@Override
		public int getCount() {
			return viewList.size();
		}

		// �жϴ�instantiateItem()��������Key�뵱ǰ��View�Ƿ�
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		// �ӵ�ǰcontainer��ɾ��ָ��λ�ã�position����View
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
			((ViewPager) container).removeView(viewList.get(position));
		}

		// instantiateItem()�����������£���һ������ǰ��ͼ��ӵ�container�У��ڶ������ص�ǰView
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

		// ��ѡ��ҳ��ʱ
		@Override
		public void onPageSelected(int selected) {
			// ѡ�����һҳʱ����ʾ��ť
			showButton(selected);
			//ѡ�е�ҳ����ʾСԲ���ý���
		 	for (int i = 0; i < mDots.length; i++) {
		 	mDots[selected].setImageResource(R.drawable.page_indicator_focused);
				//û��ѡ�е�ҳ��
		 		if (selected != i) {
		 		mDots[i].setImageResource(R.drawable.page_indicator);
				}
		 	}
		}
	}
		
		
	 

	// ѡ�����һҳʱ����ʾ��ť
	private void showButton(int selected) {
		// ��Ϊ���һҳʱ,����ʾ��ť,button��ť�Ƿ���activity_guide������
		if (selected == (resId.length - 1)) {
			mStartBtn.setVisibility(View.VISIBLE);
			// ���ð�ť����������������¼�
			mStartBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// SharedPreferences���ݱ���
					setGuideSP();
				}
			});
		} else {
			// viewPagerҳ��������ʱ��ҲҪ���ɼ�
			mStartBtn.setVisibility(View.INVISIBLE);
		}
	}

	// ���button,SP���ݱ���
	private void setGuideSP() {
		// ����isFirst����ֵ��guide�У������ж�
		SharedPreferences sp = getSharedPreferences("guide", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("isFirst", false);
		editor.commit();
		// ��ת��������
		Intent intent = new Intent(GuideActivity.this, MainActivity.class);
		startActivity(intent);
		// ���ٱ�����
		finish();
	}

}
