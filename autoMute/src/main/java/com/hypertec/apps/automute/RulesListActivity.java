package com.hypertec.apps.automute;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hypertec.apps.automute.fragments.RulesListFragment;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class RulesListActivity extends Activity {

    static final int SECTION_RULE_LIST = 0;
    static final int SECTION_SETTING = 1;
   
    private RulesListFragment mRulesListFragment;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules_list);

	FragmentManager fragmentManager = getFragmentManager();
	mRulesListFragment = new RulesListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.rules_list_content, mRulesListFragment)
                .commit();
        
        mAdView = (AdView) findViewById(R.id.rules_list_adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        
    }
    
    @Override
    protected void onPause() {
	mAdView.pause();
	super.onPause();
    }
    
    @Override
    protected void onResume() {
	super.onResume();
	mAdView.resume();
    }
    
    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {        
        getMenuInflater().inflate(R.menu.rules_list, menu);
        
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_rule_list);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(getBaseContext(), RuleEditorActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
