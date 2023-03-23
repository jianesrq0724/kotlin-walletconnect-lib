package io.walletconnect.example.utils;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.schedulers.Schedulers;


public class RxUtils {
    public RxUtils() {
    }

    public static <T> FlowableTransformer<T, T> rxSchedulerNewThread() {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(Flowable<T> tObservable) {
                return tObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io());
            }
        };
    }

}
