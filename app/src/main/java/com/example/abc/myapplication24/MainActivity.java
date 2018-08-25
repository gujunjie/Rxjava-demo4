package com.example.abc.myapplication24;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String TAG="text";

    private Subscription subscription;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button=(Button)findViewById(R.id.btn_receive);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscription.request(95);
            }
        });

        //flowable1();

        flowable2();

        flowable3();

        //backpressureStrategyError();

        //backpressureStrategyMissing();

        //backpressureStrategyBuffer();

        //backpressureStrategyDrop();

        //backpressureStrategyLatest();
    }

    public void flowable1()//响应式拉取，被观察者发送事件到缓存区（默认大小128），观察者按需求拉取事件
             //实现方法  subscription.request(n),作用对象是观察者,如果不调用该方法，观察者就不接受事件，但是被观察者可以发送事件到缓存区
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                e.onNext(1);
                Log.d(TAG, "发送事件1");
                e.onNext(2);
                Log.d(TAG, "发送事件2");
                e.onNext(3);
                Log.d(TAG, "发送事件3");
                e.onNext(4);
                Log.d(TAG, "发送事件4");
                e.onComplete();
            }
        }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {

                subscription=s;

            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "接受到事件"+integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "onError: ");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        });
    }


     //反馈控制，作用对象为被观察者，被观察者根据观察者接受事件的能力从而控制发送事件的速度
    //实现方法  flowableemitter.requseted()
    public void flowable2(){

        //同步情况

        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                Log.d(TAG, "current requested "+e.requested());

                Log.d(TAG, "发送事件1");
                e.onNext(1);
                Log.d(TAG, "current requested "+e.requested());

                Log.d(TAG, "发送事件2");
                e.onNext(2);
                Log.d(TAG, "current requested "+e.requested());

                Log.d(TAG, "发送事件3");
                e.onNext(3);
                Log.d(TAG, "current requested "+e.requested());
                
            }
        },BackpressureStrategy.ERROR).subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.d(TAG, "onSubscribe: ");
                s.request(2);

            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "onNext: "+integer);
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "onError: ",t);
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: ");
            }
        });
    }//同步

    public void flowable3()//异步
    {
             Flowable.create(new FlowableOnSubscribe<Integer>() {
                 @Override
                 public void subscribe(FlowableEmitter<Integer> e) {
                     Log.d(TAG, "current requested "+e.requested());

                     boolean flag;

                     for(int i=0;;i++)
                     {
                         flag=false;

                         while(e.requested()==0)
                         {
                             if(!flag)
                             {
                                 Log.d(TAG, " 我不能再发事件了");
                                 flag=true;
                             }
                         }
                         e.onNext(i);
                         Log.d(TAG, "事件"+i+"current requested "+e.requested());
                     }



                     
                 }
             },BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribe(new Subscriber<Integer>() {
                         @Override
                         public void onSubscribe(Subscription s) {
                             Log.d(TAG, "onSubscribe: ");
                             subscription=s;
                         }

                         @Override
                         public void onNext(Integer integer) {
                             Log.d(TAG, "onNext: "+integer);
                         }

                         @Override
                         public void onError(Throwable t) {
                             Log.d(TAG, "onError: ",t);
                         }

                         @Override
                         public void onComplete() {
                             Log.d(TAG, "onComplete: ");
                         }
                     });
    }


    public void backpressureStrategyError()//缓存区已经满了128，但是仍要发事件，直接抛出异常
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                for(int i=0;i<129;i++)
                {
                    Log.d(TAG, "发送事件"+i);
                    e.onNext(i);
                }
                e.onComplete();

            }
        },BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: ",t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void backpressureStrategyMissing()//缓存区已经满了128，但是仍要发事件，友好提示缓存区满了
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                for(int i=0;i<129;i++)
                {
                    Log.d(TAG, "发送事件"+i);
                    e.onNext(i);
                }
            }
        },BackpressureStrategy.MISSING).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: ",t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void backpressureStrategyBuffer()//将缓存区设置为无限大，但是要注意内存的变化，避免oom
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                for(int i=0;i<129;i++)
                {
                    Log.d(TAG, "发送事件"+i);
                    e.onNext(i);
                }
                e.onComplete();
            }
        },BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe: ");

                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void backpressureStrategyDrop()//发送的事件把超出缓存区的舍弃掉，如发送150个事件，超出128以后的事件全部舍弃
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                for(int i=0;i<150;i++)
                {
                    Log.d(TAG, "发送事件"+i);
                    e.onNext(i);
                }
            }
        },BackpressureStrategy.DROP).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription=s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    public void backpressureStrategyLatest()//发送的事件超出缓存区的舍弃掉，但是会保留最后一个事件，如发送150个事件，超出128以后除了最后一个事件即150个其他的都舍弃掉
                                            //也就是保留1-128 和150
    {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) {
                for(int i=0;i<150;i++)
                {
                    Log.d(TAG, "发送事件"+i);
                    e.onNext(i);
                }
                e.onComplete();
            }
        },BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription=s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext: "+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CompositeDisposable disposable=new CompositeDisposable();
        disposable.dispose();
    }
}
