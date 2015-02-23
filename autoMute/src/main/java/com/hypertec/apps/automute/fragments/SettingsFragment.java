/**
 * Copyright (c) 2014/10/20 Hypertec Corporation
 * All rights reserved. This program and the accompanying materials
 * are licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   Ryan Chou - initial API and implementation
 */
package com.hypertec.apps.automute.fragments;

import com.hypertec.apps.automute.R;
import com.hypertec.apps.automute.Utils.AutoMuteAlarmMgr;
import com.hypertec.apps.automute.Utils.Utils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * @author ryanchou
 *
 */
public class SettingsFragment extends Fragment {
    
    CompoundButton.OnCheckedChangeListener mAutoMuteServiceStatusChangeListener = new CompoundButton.OnCheckedChangeListener() 
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
            	// disable automute service -> cancel all alarms
            	Utils.SetAutoMuteStatus(false);
            	AutoMuteAlarmMgr.stopAllAlarms();
            	Utils.stopActiveRule();
            }
            else {
            	Utils.SetAutoMuteStatus(true);
            	AutoMuteAlarmMgr.startAllAlarms();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		return v;
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) 
    {
		super.onActivityCreated(savedInstanceState);
		CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.settings_disable_automute_checkbox);
		if (Utils.IsAutoMuteActive() == false) {
			checkBox.setChecked(true);
		}

		// Be careful, it must set checked before listener attached
		checkBox.setOnCheckedChangeListener(mAutoMuteServiceStatusChangeListener);
		
    }
}
