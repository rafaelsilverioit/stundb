package com.stundb.net.core.managers;

import com.stundb.net.core.managers.impl.RequestManagerImpl;

public sealed interface RequestManager permits RequestManagerImpl {

    void offer(String element);

    boolean contains(String element);
}
