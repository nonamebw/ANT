package com.lynch;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by lynch on 2019-05-05. <br>
 **/
public class RabbitMqSendTest {
    private final static String host = "rabbitmq.lynch.fun";
    private final static String QUEUE_NAME = "lora_down";
    private final static String EXCHANGE_NAME = "loraExchange";
    private final static String ROUTINGKEY = "receive-lora";
    /**
     * 线程设置
     */
    static int threads = 10;        // 运行的测试线程数
    static int runs = 1;            // 每个线程运行的次数
    static int msgNums = 0;
    static int size = 0;


    private static ConnectionFactory factory;
    private static Connection connection;


    static long sendTime = 0;
    static long recvTime = 0;

    static Integer myLock;        // 锁定一下计数器
    //    static byte[] testdata;
    static JSONObject message = new JSONObject();


    static String data = "{\"type\":\"write.req\",\"device_id\":\"9a-b6-ff-00\",\"key\":\"0.general_onoff\",\"value\":1,\"reply_to\":\"baiya.web.user.streetlamp\",\"id\":\"72e15160-5234-44ee-bc1b-d70beca8eaec\"}";


    public static void main(String[] args) throws Exception {
        //设置数据大小
//        testdata = new byte[size];
//        for(int i=0;i<size;i++){
//            testdata[i] = 'A';
//        }
        message.put("type", "write.req");
        message.put("device_id", "9a-b6-ff-00");
        message.put("key", "0.general_onoff");
        message.put("value", "1");
        message.put("reply_to", "baiya.web.user.streetlamp");
        size = message.toString().length();

        // 创建链接工程
        factory = new ConnectionFactory();
        factory.setHost(host);
        //set user and password
        factory.setUsername("admin");
        factory.setPassword("admin");
        // 创建一个新的消息队列服务器实体的连接
        connection = factory.newConnection();


        myLock = new Integer(threads);

        System.out.println("正在测试...");

        for (int i = 0; i < threads; i++) {
            new SendThread().start();
        }

    }

    private static class SendThread extends Thread {
        public void run() {
            try {
                // 创建一个新的消息读写的通道
                Channel channel = connection.createChannel();
                // 声明exchange模式并且为持久化exchange
                channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
                // declare a queue (声明一个队列)
                // 参数分别为
                // 1,队列的名字
                // 2,是否为一个持久的队列，持久的队列在服务重新启动的后依然存在
                // 3,如果为true，那么就在此次连接中建立一个独占的队列
                // 4,是否为自动删除
                // 5,队列的一些其他构造参数
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                //绑定
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTINGKEY);
                // set confirm
                channel.confirmSelect();
                //==========================发送消息开始=================================
                long startTime = System.currentTimeMillis();
                int counts = 0;
                for (int i = 0; i < runs; i++) {
                    try {
                        //发送消息  MessageProperties.PERSISTENT_TEXT_PLAIN:将消息设为持久化
                        channel.basicPublish(EXCHANGE_NAME, ROUTINGKEY, MessageProperties.PERSISTENT_TEXT_PLAIN, message.toString().getBytes());
                        counts++;
                        ///System.out.println(" [x] Sent '" + new String(testdata,"utf-8") + "'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                long endTime = System.currentTimeMillis() - startTime;
                channel.waitForConfirmsOrDie();

                synchronized (myLock) {
                    //发送消息总时间
                    sendTime += endTime;
                    msgNums += counts;

                    myLock--;
                    if (myLock.equals(0)) {
                        try {
                            channel.close();
                            connection.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("测试完成!\n启动线程数:【" + threads + "】\t每个线程发送消息数:【" + runs + "】\t所有线程实际发送消息数:【" + msgNums + "】\t发送消息包大小:【" + size + " byte】");
                        System.out.println("发送消息处理时间:【" + sendTime + " ms】\t处理发送消息速度(QPS):每秒【" + runs * threads * 1000 / sendTime + " 次】\t发送消息的平均时间:【" + sendTime / (runs * threads) + " ms】");
                    }
                }
                //==========================发送消息结束=================================
            } catch (Exception e) {

            }
        }
    }


    public static void req() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        myLock = new Integer(threads);

        channel.basicPublish("", QUEUE_NAME, null, message.toString().getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
