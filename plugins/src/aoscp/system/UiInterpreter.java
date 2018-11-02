/*
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package aoscp.system;

import android.content.Context;

import aoscp.system.overlay.ColorManager;
/*
 * A simple interpreter that invokes outer classes 
 * to manage back task UI changes.
 */
public class UiInterpreter {

    private static final String TAG = "UiInterpreter";

    private Context mContext;

    public UiInterpreter(Context context) {
        mContext = context;
    }

	public ColorManager getColorManager() {
		return ColorManager(mContext);
	}
}
