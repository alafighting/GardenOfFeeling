package sxkeji.net.dailydiary.common.activities;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.os.Process;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import sxkeji.net.dailydiary.R;
import sxkeji.net.dailydiary.beans.OpenEyeDailyBean;
import sxkeji.net.dailydiary.common.views.adapters.MainTabsVPAdapter;
import sxkeji.net.dailydiary.http.HttpClient;
import sxkeji.net.dailydiary.http.HttpResponseHandler;
import sxkeji.net.dailydiary.storage.ACache;
import sxkeji.net.dailydiary.storage.Constant;
import sxkeji.net.dailydiary.utils.GsonUtils;
import sxkeji.net.dailydiary.utils.UIUtils;

/**
 * 主页
 * Created by zhangshixin on 3/14/2016.
 */
public class MainActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tab_layout)
    TabLayout tabLayout;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.vp_tab_content)
    ViewPager vpTabContent;
    @Bind(R.id.main_content)
    CoordinatorLayout mainContent;
    @Bind(R.id.img_user)
    ImageView imgUser;
    @Bind(R.id.tv_name)
    TextView userName;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.fab_add)
    FloatingActionButton fabAdd;
    private RelativeLayout rlSelectView;
    private ActionBarDrawerToggle mDrawerToggle;
    private OpenEyeDailyBean mDailyBean;
    long[] mHits = new long[2];
    ACache aCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initViews();
        loadData();
        setListeners();
    }

    private void setListeners() {
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator.ofFloat(fabAdd, "rotation", 0f, 225f).setDuration(1000).start();
                showChooseBottomSheet();
            }
        });
    }

    /**
     * 选择要添加的类型
     */
    private void showChooseBottomSheet() {
        new BottomSheet.Builder(MainActivity.this)
                .title(getResources().getString(R.string.add_title))
                .sheet(R.menu.menu_list_add)
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.item_article:
                                //TODO: startActivityForResult 当创建成功后再查询

                                jumpToActivity(ArticleWriteActivity.class);
                                break;
                            case R.id.item_reminder:
                                break;
                            case R.id.item_cancel:
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ObjectAnimator.ofFloat(fabAdd,"rotation",225f,360f).setDuration(1000).start();
                    }
                }).show();
    }

    /**
     * 跳转
     * @param clazz
     */
    private void jumpToActivity(Class<?> clazz) {
        Intent intent = new Intent(MainActivity.this,clazz);
        startActivity(intent);
    }

    /**
     * 显示SnackBar
     * @param str
     */
    private void showSnackToast(String str) {
        if (str != null) {
            Snackbar.make(mainContent, str, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        aCache = ACache.get(this);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(mDrawerToggle);

        Intent  intent = new Intent();
        intent.resolveActivity(getPackageManager());
    }

    /**
     * 加载数据
     */
    private void loadData() {
        loadRecommandData();
        loadTabsViewPagerData();
        loadTabsData();
    }

    private void loadRecommandData() {
        Map<String,String> map = new HashMap<>();
        map.put("num", "2");
        HttpClient.builder(MainActivity.this).get(Constant.URL_OPEN_EYE_DIALY, map, new HttpResponseHandler() {
            @Override
            public void onSuccess(String content) {
                super.onSuccess(content);
                if (!TextUtils.isEmpty(content)) {
                    aCache.put(Constant.OPEN_EYE_DATA, content);
                }
//                mDailyBean = GsonUtils.fromJson(content, OpenEyeDailyBean.class);

                Log.e("loadRecommandData", content.toString());
            }

            @Override
            public void onFailure(Request request, IOException e) {
                super.onFailure(request, e);
            }
        });
    }

    /**
     * 加载Tab列表的viewPager的内容
     */
    private void loadTabsViewPagerData() {
        MainTabsVPAdapter mTabsVPAdapter = new MainTabsVPAdapter(getSupportFragmentManager());
        mTabsVPAdapter.addFragment(new HomeFragment(), "全部");
        mTabsVPAdapter.addFragment(new HomeFragment(), "文件夹");
        mTabsVPAdapter.addFragment(new RecommandFragment(), "推荐");
        vpTabContent.setAdapter(mTabsVPAdapter);
    }

    /**
     * 加载Tab列表
     */
    private void loadTabsData() {
        for (int i = 0; i < 3; i++) {
            tabLayout.addTab(tabLayout.newTab());
        }
        tabLayout.setupWithViewPager(vpTabContent);
    }

    /**
     * 设置选中背景色，关闭抽屉
     * @param rlSelect
     */
    private void changeBgAndCloseDrawer(RelativeLayout rlSelect) {
        if(rlSelectView != null){
            rlSelectView.setBackgroundColor(Color.WHITE);
        }
        rlSelectView = rlSelect;
        rlSelectView.setBackgroundColor(Color.parseColor("#eeeeee"));
        if(drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }
    }

    @Override
    public void
    onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        UIUtils.showToastSafe(MainActivity.this, "再次点击退出365记");

        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();

        if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {

            System.exit(0);
            android.os.Process.killProcess(Process.myPid());

        }
    }
}