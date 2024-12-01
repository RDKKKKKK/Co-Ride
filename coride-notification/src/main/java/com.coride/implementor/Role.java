package com.coride.implementor;

import javax.websocket.Session;
import java.io.IOException;

public interface Role {

    public String sendConfirmation() throws IOException;

    public String sendResult() throws IOException;

}

