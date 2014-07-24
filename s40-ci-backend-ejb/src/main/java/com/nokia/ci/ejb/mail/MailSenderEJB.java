package com.nokia.ci.ejb.mail;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.BackendMessagingException;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stateless session bean for sending E-mail.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class MailSenderEJB implements Serializable {

    public static enum EmailPriority {

        PRIORITY_HIGH, PRIORITY_NORMAL, PRIOIRTY_LOW;
    }
    private static final Logger log = LoggerFactory.getLogger(MailSenderEJB.class);
    private static final String EMAIL_NEWLINE_CHAR_DEFAULT = "\n";
    @Resource(mappedName = "java:jboss/mail/CI20SMTPService")
    private Session mailSession;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private SysUserEJB sysUserEJB;

    /**
     * Sends email with given subject and body message.
     *
     * @param user SysUser to send the mail to
     * @param subject Subject of the message.
     * @param message Body text of the message.
     * @throws BackendMessagingException If message sending fails.
     */
    public void sendMail(SysUser user, String subject, String message) throws BackendMessagingException {
        sendMail(user, subject, message, EmailPriority.PRIORITY_NORMAL);
    }

    public void sendMail(SysUser user, String subject, String message, EmailPriority priority) throws BackendMessagingException {
        List<String> receiversList = new ArrayList<String>();
        receiversList.add(user.getEmail());
        sendMail(receiversList, subject, message, priority);
    }

    /**
     * Sends email with given subject and body message.
     *
     * @param users List of sysusers to send the mail
     * @param subject Subject of the message.
     * @param message Body text of the message.
     * @throws BackendMessagingException If message sending fails.
     */
    public void sendMailSysUsers(List<SysUser> users, String subject, String message) throws BackendMessagingException {
        sendMailSysUsers(users, subject, message, EmailPriority.PRIORITY_NORMAL);
    }

    public void sendMailSysUsers(List<SysUser> users, String subject, String message, EmailPriority priority) throws BackendMessagingException {
        List<String> receiversList = new ArrayList<String>();
        for (SysUser user : users) {
            receiversList.add(user.getEmail());
        }
        sendMail(receiversList, subject, message, priority);
    }

    /**
     * Sends email with given subject and body message.
     *
     * @param emails Comma separated list of receivers.
     * @param subject Subject of the message.
     * @param message Body text of the message.
     * @throws BackendMessagingException If message sending fails.
     */
    public void sendMail(String emails, String subject, String message) throws BackendMessagingException {
        sendMail(emails, subject, message, EmailPriority.PRIORITY_NORMAL);
    }

    public void sendMail(String emails, String subject, String message, EmailPriority priority) throws BackendMessagingException {
        List<String> receiversList = new ArrayList<String>();
        String[] receiversArray = emails.split("[,;\\s+]");
        if (receiversArray != null) {
            receiversList = Arrays.asList(receiversArray);
        }
        sendMail(receiversList, subject, message, priority);
    }

    /**
     * Sends email with given subject and body message.
     *
     * @param emails List of receivers.
     * @param subject Subject of the message.
     * @param message Body text of the message
     * @throws BackendMessagingException If message sending fails.
     */
    public void sendMail(List<String> emails, String subject, String message) throws BackendMessagingException {
        sendMail(emails, subject, message, EmailPriority.PRIORITY_NORMAL);
    }

    public void sendMail(List<String> emails, String subject, String message, EmailPriority priority) throws BackendMessagingException {
        long startTime = System.currentTimeMillis();

        // Remove duplicates
        Set<String> receivers = new HashSet<String>();
        receivers.addAll(emails);

        List<SysUser> users = sysUserEJB.readAll();
        for (SysUser u : users) {
            if (u.getSendEmail() == null || u.getSendEmail() == true) {
                continue;
            } else if (receivers.contains(u.getEmail())) {
                log.info("User {} does not want email notification, removing from receivers", u);
                receivers.remove(u.getEmail());
            }
        }

        if (receivers.isEmpty()) {
            return;
        }

        log.info("Sending email message with subject '{}' to {}", subject, receivers);

        Address sender;
        try {
            sender = getSenderAddress();
        } catch (AddressException ae) {
            log.error("Invalid sender address can not send email!", ae);
            throw new BackendMessagingException(ae);
        }

        String msg = message;
        String emailNewline = sysConfigEJB.getValue(SysConfigKey.EMAIL_NEWLINE_CHAR, EMAIL_NEWLINE_CHAR_DEFAULT);
        msg += emailNewline + emailNewline;
        msg += "Please do not reply to this message.";

        MimeMessage mimeMessage = new MimeMessage(mailSession);
        try {
            mimeMessage.setFrom(sender);
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(msg, "text/plain");
            if (priority == EmailPriority.PRIORITY_HIGH) {
                mimeMessage.setHeader("X-Priority", "1");
            } else if (priority == EmailPriority.PRIOIRTY_LOW) {
                mimeMessage.setHeader("X-Priority", "5");
            }
        } catch (MessagingException me) {
            log.error("Email sending failed!", me);
            throw new BackendMessagingException(me);
        }

        // Send emails one by one as the implementation will throw MessagingException
        // if any of the addresses is not in use
        for (String address : receivers) {
            try {
                Address[] to = getReceiverAddress(address);
                if (to == null) {
                    continue;
                }

                if (log.isDebugEnabled()) {
                    logDebugMessage(mimeMessage);
                }

                mimeMessage.setRecipients(Message.RecipientType.TO, to);
                Transport.send(mimeMessage);
            } catch (MessagingException me) {
                log.error("Could not send email!", me);
            }
        }

        log.info("Email send successfully in {}ms", System.currentTimeMillis() - startTime);
    }

    private Address[] getReceiverAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }

        List<Address> receiverList = new ArrayList<Address>();
        try {
            receiverList.add(new InternetAddress(address));
        } catch (AddressException ex) {
            log.warn("Invalid receiver email address {}, address is ignored! Cause: {}", address, ex.getMessage());
            return null;
        }

        return receiverList.toArray(new Address[0]);
    }

    private Address getSenderAddress() throws AddressException {
        String senderString = sysConfigEJB.getValue(SysConfigKey.EMAIL_SENDER_ADDRESS,
                "ci20_not_set@noreply.nokia.com");
        return new InternetAddress(senderString);
    }

    private void logDebugMessage(MimeMessage mimeMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("-----------------------------").append("\n");
        try {
            if (mimeMessage == null) {
                log.debug("Null Message!");
                return;
            }
            sb.append("From: ");
            if (mimeMessage.getFrom() != null) {
                sb.append(Arrays.asList(mimeMessage.getFrom()).toString()).append("\n");
            }
            sb.append("To: ");
            if (mimeMessage.getAllRecipients() != null) {
                sb.append(Arrays.asList(mimeMessage.getAllRecipients()).toString()).append("\n");
            }
            sb.append("Subject: ").append(mimeMessage.getSubject()).append("\n");
            sb.append("Content-Type: ").append(mimeMessage.getContentType()).append("\n");
            sb.append("Content: ").append(mimeMessage.getContent()).append("\n");
        } catch (IOException ioe) {
            log.debug("Can not get content of message! Cause: {}", ioe.getMessage());
        } catch (MessagingException me) {
            log.debug("Debug failed! Cause: {}", me.getMessage());
        } finally {
            sb.append("-----------------------------").append("\n");
        }
        log.debug(sb.toString());
    }
}
