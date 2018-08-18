package com.atguigu.gmall.payment;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;
import javax.xml.soap.Text;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Test
	public void contextLoads() {
	}

	/*@Test
	public void testActiveMQ() throws JMSException {
		ConnectionFactory connectionFactory =
				new ActiveMQConnectionFactory("tcp://192.168.116.129:61616");
		Connection connection = connectionFactory.createConnection();

		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue queue = session.createQueue("atguigu");

		MessageProducer producer = session.createProducer(queue);

		ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();

		activeMQTextMessage.setText("hello !!!");

		producer.send(activeMQTextMessage);

		session.close();

		producer.close();

		connection.close();
	}*/
	/*@Test
	public void testActiveMQ2() throws JMSException {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.116.129:61616");

		Connection connection = activeMQConnectionFactory.createConnection();
		connection.start();

		Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

		Queue queue = session.createQueue("atguigu");

		MessageConsumer consumer = session.createConsumer(queue);

		consumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) {
				if(message instanceof TextMessage){
					try {
						String text = ((TextMessage) message).getText();
						System.out.println("text:" + text);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}*/
}
