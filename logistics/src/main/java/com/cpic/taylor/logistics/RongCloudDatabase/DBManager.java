package com.cpic.taylor.logistics.RongCloudDatabase;

import android.content.Context;

public class DBManager {

    private static DBManager instance;
    private com.cpic.taylor.logistics.RongCloudDatabase.DaoMaster daoMaster;
    private DaoSession daoSession;

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager(context);
                }
            }
        }
        return instance;
    }

    private DBManager(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                com.cpic.taylor.logistics.RongCloudDatabase.DaoMaster.OpenHelper helper = new com.cpic.taylor.logistics.RongCloudDatabase.DaoMaster.DevOpenHelper(context, context.getPackageName(), null);
                daoMaster = new com.cpic.taylor.logistics.RongCloudDatabase.DaoMaster(helper.getWritableDatabase());
            }
            daoSession = daoMaster.newSession();
        }
    }

    public com.cpic.taylor.logistics.RongCloudDatabase.DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public void setDaoMaster(DaoMaster daoMaster) {
        this.daoMaster = daoMaster;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }
}
