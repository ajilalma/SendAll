package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.broadcastreceiver.AbstractGroupCreationListener;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by ajilal on 26/7/17.
 */

abstract public class AbstractClientConnector implements Runnable, Updatable {
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected Updatable updatableActivity;
    protected AppManager appManager;

    public AbstractClientConnector(Socket socket, Updatable updatableActivity, AppManager appManager){
        this.socket = socket;
        this.appManager = appManager;
        this.updatableActivity = updatableActivity;
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            subRun();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    abstract protected void subRun();

    public void closeSocket(){
        try {
            dataInputStream.close();
            dataOutputStream.close();

            if(!socket.isClosed())
                socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
