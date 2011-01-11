/**
 *  Libretto Universitario
 *  Copyright (C) 2010 Roberto Leinardi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package com.leinardi.ubuntucountdownwidget.misc;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Constants {
    
    public static final String FORCE_WIDGET_UPDATE = "com.leinardi.ubuntucountdownwidget.FORCE_WIDGET_UPDATE";
    
    public static final GregorianCalendar ubuntuReleaseDay = new GregorianCalendar(2011, Calendar.APRIL, 28);
	
	public static final String PREF_CHANGELOG = "changelog";
	public static final String PREF_APP_VERSION = "app.version";

	public static final String PREF_EULA = "eula";
	public static final String PREF_EULA_ACCEPTED = "eula.accepted";
	
	public static final String IS_THE_FIRST_RUN = "first.run";

	public static final String APP_NAMESPACE = "http://schemas.android.com/apk/res/com.leinardi.ubuntucountdownwidget";
}