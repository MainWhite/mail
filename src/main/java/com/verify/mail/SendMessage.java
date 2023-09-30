package com.verify.mail;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author 白
 */
@RestController
public class SendMessage {
	private static final String USERNAME = "3224886279@qq.com";
	private static final String AUTHORIZATION = "bwbkfjnigakqdaai";

	private static Session getMailSession() {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.126.com");
		//126——25
		//163——645
		//qq——587
		props.put("mail.smtp.port", 587);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enale", "true");
		props.put("mail.smtp.user", USERNAME);
		props.put("mail.transport.protocol", "smtp");

		return Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, AUTHORIZATION);
			}
		});
	}

	/**
	 * 发送纯文本邮件html格式
	 * @param receptionMail 邮件收件人 多个收件人例子 ?receptionMail=收件人地址,收件人地址
	 * @param subject 邮件主题
	 * @param text 邮件主体
	 */
	@RequestMapping("/sendTextMail")
	public void sendTextMail(String receptionMail, String subject, String text) {
		Session session = getMailSession();
		MimeMessage message = new MimeMessage(session);
		try {
			message.setSubject(subject);
			message.setText(text,"utf-8", "html");
			message.setFrom(new InternetAddress(USERNAME));
			InternetAddress[] in=InternetAddress.parse(receptionMail);
			Address[] addresses=new Address[in.length];
			for (int i = 0; i < in.length; i++) {
				addresses[i]=new InternetAddress(in[i].toString());
			}
			message.setRecipients(Message.RecipientType.TO, addresses);
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 发送带image图片的邮件
	 * @param receptionMail 收件人地址 多个收件人例子 ?receptionMail=收件人地址,收件人地址
	 * @param subject 邮件主题
	 * @param htmlText 邮件主体html格式
	 * @param imageName image显示的名字
	 * @param imageAddress image地址
	 * @param imageId imageid要和邮件主体中的id相同
	 * @param imageUrl image地址是否为本地地址 true表示不是本地
	 */
	@RequestMapping("/sendImageMail")
	public void sendImageMail(String receptionMail, @RequestParam(defaultValue = "白的系统通知",required = false) String subject, String htmlText,@RequestParam(defaultValue = "@author 白 qq:3224886279",required = false) String imageName, String imageAddress, String imageId,Boolean imageUrl) {
		// 创建Session会话
		Session session = getMailSession();
		try {
			// 创建邮件对象
			MimeMessage message = new MimeMessage(session);
			//邮件主题
			message.setFrom(new InternetAddress(USERNAME));
			//多个收件人处理
			InternetAddress[] in=InternetAddress.parse(receptionMail);
			Address[] addresses=new Address[in.length];
			for (int i = 0; i < in.length; i++) {
				addresses[i]=new InternetAddress(in[i].toString());
			}
			message.setRecipients(Message.RecipientType.TO, addresses);
			message.setSubject(subject);

			// 邮件正文
			BodyPart textPart = new MimeBodyPart();
			textPart.setContent(htmlText, "text/html;charset=utf-8");

			//附件
			BodyPart imagePart = new MimeBodyPart();
			imagePart.setFileName(imageName);
			//上传图片文件
			if (imageUrl) {
				imagePart.setDataHandler(new DataHandler(new URLDataSource(
						new URL(imageAddress))));
			} else {
				imagePart.setDataHandler(new DataHandler(new ByteArrayDataSource(
						Files.readAllBytes(Paths.get(imageAddress)), "application/octet-stream")));
			}
			//图片的内容id
			imagePart.setHeader("Content-ID", imageId);

			//将正文+附件组装成Multipart对象
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(textPart);
			multipart.addBodyPart(imagePart);
			// 放入邮件
			message.setContent(multipart);

			// 发送邮件
			Transport.send(message);

		} catch (MessagingException | IOException e) {
			e.printStackTrace();
		}

	}
}
