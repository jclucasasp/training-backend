package org.lucas.arbackend.service.messaging;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String fromEmail;

    @Value("${spring.redirect.payment_url}")
    String paymentUrl;

    @Value(("${spring.redirect.login_url}"))
    String loginUrl;

    // TODO: Change the redirect url's when the frontend is done
    public void sendEmail(String toEmail, String fullName, String otp, CustomEmailType entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("CustomEmailType type cannot be null");
        }

        switch (entityType) {
            case RESET -> {
                if (otp != null) {
                    sendPasswordResetEmail(toEmail, fullName, otp);
                } else {
                    throw new IllegalArgumentException("OTP cannot be null");
                }
            }

            case WELCOME -> sendStandardWelcome(toEmail, fullName);
            case SUBSCRIPTION_REMINDER -> sendOrgSubscriptionReminder(toEmail, fullName);
            case ORG_WELCOME -> sendOrgWelcome(toEmail, fullName);
            default -> throw new IllegalArgumentException("Invalid CustomEmailType type");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String fullName, String otp) {
        log.info("Sending email to: [{}] - [{}] - Name: [{}]", toEmail, otp, "OTP");

        String subject = "LuminoEd: Password reset OTP";
        String htmlTemplate = getOtpTemplate()
                .replace("{{FULL_NAME}}", fullName)
                .replace("{{OTP}}", otp)
                .replace("{{LOGIN_URL}}", loginUrl);

        executeDispatch(toEmail, subject, htmlTemplate);
    }

    public void sendOrgSubscriptionReminder(String toEmail, String fullName) {
        String subject = "LuminoEd: Activate Your LuminoEd Enterprise Workspace";
        String htmlTemplate = getOrgSubscriptionReminderTemplate()
                .replace("{{FULL_NAME}}", fullName)
                .replace("{{PAYMENT_URL}}", paymentUrl);

        executeDispatch(toEmail, subject, htmlTemplate);
    }

    public void sendOrgWelcome(String toEmail, String fullName) {
        String subject = "LuminoEd: Access Your Spatial Training Portal";
        String htmlTemplate = getOrgWelcomeTemplate()
                .replace("{{FULL_NAME}}", fullName)
                .replace("{{LOGIN_URL}}", loginUrl);

        executeDispatch(toEmail, subject, htmlTemplate);
    }

    private void sendStandardWelcome(String toEmail, String fullName) {
        String subject = "LuminoEd: Access Your Spatial Training Portal";
        String htmlTemplate = getWelcomeTemplate()
                .replace("{{FULL_NAME}}", fullName)
                .replace("{{LOGIN_URL}}", loginUrl);

        executeDispatch(toEmail, subject, htmlTemplate);
    }

    private void executeDispatch(String toEmail, String subject, String htmlTemplate) {
        try {
            MimeMessage mimeMailMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMailMessage, true,  "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlTemplate, true);

            mailSender.send(mimeMailMessage);
        } catch (MessagingException m) {
            log.error("Failed to send email to [{}]", toEmail, m);
            throw new RuntimeException("Failed to send email to [{}]", m);
        }
        log.info("Email send successfully to [{}]", toEmail);
    }

   private String getOrgSubscriptionReminderTemplate() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Complete Your Setup</title>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background: linear-gradient(to bottom, #f0f7ff, #f8fafc); }
                        .container { max-width: 600px; margin: 30px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); border: 1px solid #e2e8f0; }
                        .header { background: linear-gradient(135deg, #0f172a, #1e3a8a); padding: 50px 20px; text-align: center; color: white; }
                        .header h1 { font-size: 30px; margin: 12px 0 8px; font-weight: 700; letter-spacing: -0.5px; }
                        .header p { font-size: 16px; opacity: 0.9; margin: 0; }
                        .content { padding: 40px; text-align: left; color: #334155; background: #ffffff; }
                        .content h2 { font-size: 22px; color: #0f172a; margin: 0 0 15px; font-weight: 600; }
                        .content p { font-size: 15px; line-height: 1.7; margin-bottom: 20px; color: #475569; }
                        .feature-list { margin: 25px 0; padding: 0; list-style: none; }
                        .feature-item { font-size: 15px; margin-bottom: 12px; color: #334155; padding-left: 28px; position: relative; }
                        .feature-item::before { content: "✦"; position: absolute; left: 0; color: #38bdf8; font-weight: bold; }
                        .cta-container { text-align: center; margin: 35px 0 15px; }
                        .cta { display: inline-block; background: linear-gradient(135deg, #0284c7, #0369a1); color: white !important; font-weight: bold; font-size: 16px; padding: 16px 40px; border-radius: 8px; text-decoration: none; box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3); }
                        .footer { background: #f8fafc; padding: 30px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; }
                        .footer a { color: #0284c7; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div style="font-size: 28px; font-weight: 800; letter-spacing: 1px; color: #38bdf8;">LuminoEd</div>
                            <h1>Unlock Your Spatial Environments</h1>
                            <p>One final step to deploy high-fidelity PC VR training.</p>
                        </div>
                        <div class="content">
                            <h2>Hello {{FULL_NAME}},</h2>
                            <p>Thank you for creating your LuminoEd enterprise profile. Your account framework is ready, and you are just one step away from deploying our photorealistic training environment.</p>
                            <p>To initialize your access, download our tailored runtime application, and deploy assets across your organization, please choose a subscription tier that aligns with your hardware deployment goals.</p>
                            <p><strong>What is waiting in your environment:</strong></p>
                            <ul class="feature-list">
                                <li class="feature-item"><strong>Gaussian Splatting Visuals:</strong> Experience immersive scenarios rendered with true-to-life, photorealistic environmental replication.</li>
                                <li class="feature-item"><strong>Custom Unity Engine Runtime:</strong> Gain deployment rights to download our optimized execution pipeline built explicitly for stable, low-latency enterprise training.</li>
                                <li class="feature-item"><strong>PC VR Architecture:</strong> Unlock advanced high-performance rendering configurations built to take absolute advantage of premium head-mounted displays.</li>
                            </ul>
                            <p>Click below to choose your plan and unlock your organization's deployment pipeline:</p>
                            <div class="cta-container">
                                <a href="{{PAYMENT_URL}}" class="cta">Select Subscription Plan</a>
                            </div>
                        </div>
                        <div class="footer">
                            LuminoEd Enterprise | South Africa | <a href="https://www.luminoed.com/privacy">Privacy Policy</a><br><br>
                            If you closed your onboarding window by mistake, this link will return you directly to secure checkout configurations.<br><br>
                            © 2026 LuminoEd. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOrgWelcomeTemplate() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Workspace Activated</title>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background: linear-gradient(to bottom, #f0f7ff, #f8fafc); }
                        .container { max-width: 600px; margin: 30px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); border: 1px solid #e2e8f0; }
                        .header { background: linear-gradient(135deg, #0284c7, #0f172a); padding: 50px 20px; text-align: center; color: white; }
                        .header h1 { font-size: 30px; margin: 12px 0 8px; font-weight: 700; letter-spacing: -0.5px; }
                        .content { padding: 40px; text-align: left; color: #334155; background: #ffffff; }
                        .content h2 { font-size: 22px; color: #0f172a; margin: 0 0 15px; font-weight: 600; }
                        .content p { font-size: 15px; line-height: 1.7; margin-bottom: 20px; color: #475569; }
                        .step-list { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px 20px 20px 40px; margin: 25px 0; }
                        .step-item { font-size: 15px; margin-bottom: 12px; color: #334155; line-height: 1.6; }
                        .cta-container { text-align: center; margin: 35px 0 15px; }
                        .cta { display: inline-block; background: linear-gradient(135deg, #0f172a, #1e3a8a); color: white !important; font-weight: bold; font-size: 16px; padding: 16px 40px; border-radius: 8px; text-decoration: none; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.2); }
                        .footer { background: #f8fafc; padding: 30px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div style="font-size: 28px; font-weight: 800; letter-spacing: 1px; color: #38bdf8;">LuminoEd</div>
                            <h1>Workspace Fully Activated</h1>
                        </div>
                        <div class="content">
                            <h2>Welcome Aboard, {{FULL_NAME}}!</h2>
                            <p>Your subscription configuration is complete, and your enterprise tenant environment has been securely initialized.</p>
                            <p>You can now log into your console to manage client seats, provision team assignments, and coordinate your deployment targets.</p>
                            <p><strong>Next steps for deployment:</strong></p>
                            <ol class="step-list">
                                <li class="step-item"><strong>Log into the Management Console:</strong> Authenticate with your registered organization profile parameters.</li>
                                <li class="step-item"><strong>Download Unity Platform Engine:</strong> Navigate to the downloads directory to pull down your customized high-performance rendering executable.</li>
                                <li class="step-item"><strong>Initialize High-Fidelity Assets:</strong> Connect your PC VR hardware configuration setups to initialize stream environments utilizing photorealistic Gaussian Splats.</li>
                            </ol>
                            <div class="cta-container">
                                <a href="{{LOGIN_URL}}" class="cta">Launch Enterprise Console</a>
                            </div>
                        </div>
                        <div class="footer">
                            LuminoEd Enterprise | South Africa | Need hardware configuration support or runtime optimization assistance? Reply directly to our developer helpdesk.
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getWelcomeTemplate() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to LuminoEd</title>
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background: linear-gradient(to bottom, #f0f7ff, #f8fafc); }
                        .container { max-width: 600px; margin: 30px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08); border: 1px solid #e2e8f0; }
                        .header { background: linear-gradient(135deg, #0f172a, #1e3a8a); padding: 50px 20px; text-align: center; color: white; }
                        .header h1 { font-size: 30px; margin: 12px 0 8px; font-weight: 700; letter-spacing: -0.5px; }
                        .content { padding: 40px; text-align: left; color: #334155; background: #ffffff; }
                        .content h2 { font-size: 22px; color: #0f172a; margin: 0 0 15px; font-weight: 600; }
                        .content p { font-size: 15px; line-height: 1.7; margin-bottom: 20px; color: #475569; }
                        .step-list { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px 20px 20px 40px; margin: 25px 0; }
                        .step-item { font-size: 15px; margin-bottom: 12px; color: #334155; }
                        .cta-container { text-align: center; margin: 35px 0 15px; }
                        .cta { display: inline-block; background: linear-gradient(135deg, #0284c7, #0369a1); color: white !important; font-weight: bold; font-size: 16px; padding: 16px 40px; border-radius: 8px; text-decoration: none; box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3); }
                        .footer { background: #f8fafc; padding: 30px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #f1f5f9; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div style="font-size: 28px; font-weight: 800; letter-spacing: 1px; color: #38bdf8;">LuminoEd</div>
                            <h1>Welcome to Spatial Training</h1>
                        </div>
                        <div class="content">
                            <h2>Hello {{FULL_NAME}},</h2>
                            <p>Your institutional profile has been successfully provisioned. You have been granted full access to the LuminoEd interactive core training environments.</p>
                            <p>Our training engine completely shifts professional education away from standard flat interfaces into high-fidelity PC VR simulations, using photorealistic Gaussian splats to closely replicate live real-world field settings.</p>
                            <p><strong>To launch your interactive workspace environment:</strong></p>
                            <ol class="step-list">
                                <li class="step-item">Click the access credential button below to log in to your custom profile console web link.</li>
                                <li class="step-item">Download the custom Unity application engine platform directly via your profile control center dashboards.</li>
                                <li class="step-item">Initialize your PC VR headset configuration settings to enter and manipulate hyper-realistic 3D curriculum models.</li>
                            </ol>
                            <div class="cta-container">
                                <a href="{{LOGIN_URL}}" class="cta">Sign In & Download Engine</a>
                            </div>
                        </div>
                        <div class="footer">
                            LuminoEd Enterprise | South Africa | Dedicated spatial performance builds optimized for professional hardware training suites.
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String getOtpTemplate() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Reset Your LuminoEd Password</title>
                    <style>
                        body {\s
                            font-family: 'Segoe UI', Arial, sans-serif;\s
                            margin: 0;\s
                            padding: 0;\s
                            background: linear-gradient(to bottom, #f0f7ff, #f8fafc);\s
                        }
                        .container {\s
                            max-width: 600px;\s
                            margin: 30px auto;\s
                            background: white;\s
                            border-radius: 16px;\s
                            overflow: hidden;\s
                            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);\s
                            border: 1px solid #e2e8f0;
                        }
                        .header {\s
                            background: linear-gradient(135deg, #0f172a, #1e3a8a);\s
                            padding: 50px 20px;\s
                            text-align: center;\s
                            color: white;\s
                        }
                        .header h1 {\s
                            font-size: 32px;\s
                            margin: 12px 0 8px;\s
                            font-weight: 700;\s
                            letter-spacing: -0.5px;
                        }
                        .content {\s
                            padding: 40px 40px;\s
                            text-align: center;\s
                            color: #334155;\s
                            background: #ffffff;\s
                        }
                        .content p {\s
                            font-size: 16px;\s
                            line-height: 1.7;\s
                            margin-bottom: 20px;\s
                            color: #475569;
                        }
                        .big-otp {\s
                            font-size: 42px;\s
                            font-weight: bold;\s
                            letter-spacing: 10px;\s
                            color: #0284c7;\s
                            background: #f0f9ff;\s
                            padding: 20px 15px;\s
                            border-radius: 12px;\s
                            display: inline-block;\s
                            margin: 25px 0;\s
                            border: 2px dashed #38bdf8;\s
                        }
                        .warning {\s
                            color: #ef4444;\s
                            font-weight: bold;\s
                            font-size: 16px;\s
                            margin: 25px 0 15px;\s
                        }
                        .cta {\s
                            display: inline-block;\s
                            background: linear-gradient(135deg, #0284c7, #0369a1);\s
                            color: white !important;\s
                            font-weight: bold;\s
                            font-size: 16px;\s
                            padding: 16px 40px;\s
                            border-radius: 8px;\s
                            text-decoration: none;\s
                            box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3);\s
                            margin-top: 10px;
                        }
                        .footer {\s
                            background: #f8fafc;\s
                            padding: 30px;\s
                            text-align: center;\s
                            font-size: 12px;\s
                            color: #94a3b8;\s
                            border-top: 1px solid #f1f5f9;
                        }
                        .footer a {\s
                            color: #0284c7;\s
                            text-decoration: none;\s
                        }
                        .footer a:hover {
                            text-decoration: underline;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div style="font-size: 28px; font-weight: 800; letter-spacing: 1px; color: #38bdf8;">LuminoEd</div>
                            <h1>Password Reset Request</h1>
                        </div>
                        <div class="content">
                            <p>{{FULL_NAME}}, use the temporary secure verification code below to authorize your password modifications:</p>
                            <div class="big-otp">{{OTP}}</div>
                            <p class="warning">⚠️ This security verification window expires in 10 minutes.</p>
                            <p>Input this code into the access verification screen to securely update your credentials and regain entry to your 3D training portal.</p>
                            <a href={{RESET_URL}} class="cta">Verify & Reset Password</a>
                        </div>
                        <div class="footer">
                            LuminoEd Enterprise | South Africa | <a href="https://www.luminoed.com/privacy">Privacy Policy</a><br><br>
                            Didn’t request this account change? You can safely ignore this notification — your credentials remain encrypted and secure.<br><br>
                            © 2026 LuminoEd. All rights reserved.
                        </div>
                    </div>
                </body>
                </html>
        """;
    }
}
