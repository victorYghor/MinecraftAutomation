package net.kdt.pojavlaunch.profiles;

import android.graphics.drawable.Drawable;

/** Class with general information about one profile
 *
 */
public class ProfileAdapterExtra {
    public final int id;
    public final int name;
    public final Drawable icon;

    public ProfileAdapterExtra(int id, int name, Drawable icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }
}
