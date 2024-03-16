package com.stundb.modules.providers;

import com.stundb.api.btree.BTree;
import com.stundb.api.btree.BTreeImpl;

import jakarta.inject.Provider;

import java.util.Optional;

public class BTreeProvider<T> implements Provider<BTree<String, T>> {

    private BTreeImpl<String, T> instance;

    @Override
    public BTree<String, T> get() {
        instance = Optional.ofNullable(instance).orElseGet(BTreeImpl::new);
        return instance;
    }
}
