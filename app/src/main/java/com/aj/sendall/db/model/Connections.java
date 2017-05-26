package com.aj.sendall.db.model;

import org.greenrobot.greendao.annotation.*;

import java.util.List;
import com.aj.sendall.db.dao.DaoSession;
import org.greenrobot.greendao.DaoException;

import com.aj.sendall.db.dao.ConnectionsDao;
import com.aj.sendall.db.dao.PersonalInteractionDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "CONNECTIONS".
 */
@Entity(active = true)
public class Connections {

    @Id(autoincrement = true)
    private Long connectionId;

    @NotNull
    private String connectionName;

    @NotNull
    private String SSID;

    @NotNull
    private String password;

    /** Used to resolve relations */
    @Generated
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated
    private transient ConnectionsDao myDao;

    @ToMany(joinProperties = {
        @JoinProperty(name = "connectionId", referencedName = "connectionId")
    })
    private List<PersonalInteraction> personalInters;

    @Generated
    public Connections() {
    }

    public Connections(Long connectionId) {
        this.connectionId = connectionId;
    }

    @Generated
    public Connections(Long connectionId, String connectionName, String SSID, String password) {
        this.connectionId = connectionId;
        this.connectionName = connectionName;
        this.SSID = SSID;
        this.password = password;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getConnectionsDao() : null;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(Long connectionId) {
        this.connectionId = connectionId;
    }

    @NotNull
    public String getConnectionName() {
        return connectionName;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setConnectionName(@NotNull String connectionName) {
        this.connectionName = connectionName;
    }

    @NotNull
    public String getSSID() {
        return SSID;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSSID(@NotNull String SSID) {
        this.SSID = SSID;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    @Generated
    public List<PersonalInteraction> getPersonalInters() {
        if (personalInters == null) {
            __throwIfDetached();
            PersonalInteractionDao targetDao = daoSession.getPersonalInteractionDao();
            List<PersonalInteraction> personalIntersNew = targetDao._queryConnections_PersonalInters(connectionId);
            synchronized (this) {
                if(personalInters == null) {
                    personalInters = personalIntersNew;
                }
            }
        }
        return personalInters;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated
    public synchronized void resetPersonalInters() {
        personalInters = null;
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void delete() {
        __throwIfDetached();
        myDao.delete(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void update() {
        __throwIfDetached();
        myDao.update(this);
    }

    /**
    * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
    * Entity must attached to an entity context.
    */
    @Generated
    public void refresh() {
        __throwIfDetached();
        myDao.refresh(this);
    }

    @Generated
    private void __throwIfDetached() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
    }

}
