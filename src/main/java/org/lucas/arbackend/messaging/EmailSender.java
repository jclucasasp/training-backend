package org.lucas.arbackend.messaging;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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

    public void sendEmail(String toEmail, String fullName, String otp) {
        String htmlTemplate = null;
        String subject = null;

        if (otp != null) {
            subject = "LuminoEd - Password reset OTP";
            htmlTemplate = getOtpTemplate()
                    .replace("{{FULL_NAME}}", fullName)
                    .replace("{{OTP}}", otp);
        } else {
            subject = "LuminoEd - Welcome aboard";
            htmlTemplate = getWelcomeTemplate()
                    .replace("{{FULL_NAME}}", fullName);
        }
        log.info("Sending email to: [{}] - [{}] - Name: [{}]", toEmail, otp != null ? "OTP"  : "Welcome", fullName);

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

    private String getWelcomeTemplate() {
        return """
                <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Welcome to LuminoEd</title>
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
                                .header p {\s
                                    font-size: 16px;\s
                                    opacity: 0.9;\s
                                    margin: 0;\s
                                    font-weight: 400;
                                }
                                .content {\s
                                    padding: 40px 40px;\s
                                    text-align: left;\s
                                    color: #334155;\s
                                    background: #ffffff;\s
                                }
                                .content h2 {\s
                                    font-size: 22px;\s
                                    color: #0f172a;\s
                                    margin: 0 0 15px;\s
                                    font-weight: 600;
                                }
                                .content p {\s
                                    font-size: 15px;\s
                                    line-height: 1.7;\s
                                    margin-bottom: 20px;\s
                                    color: #475569;
                                }
                                .feature-list {
                                    margin: 25px 0;
                                    padding: 0;
                                    list-style: none;
                                }
                                .feature-item {
                                    font-size: 15px;
                                    margin-bottom: 12px;
                                    color: #334155;
                                    padding-left: 28px;
                                    position: relative;
                                }
                                .feature-item::before {
                                    content: "✦";
                                    position: absolute;
                                    left: 0;
                                    color: #38bdf8;
                                    font-weight: bold;
                                }
                                .cta-container {
                                    text-align: center;
                                    margin: 35px 0 15px;
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
                                    <h1>Your spatial learning journey begins.</h1>
                                    <p>Immersive professional development, unlocked.</p>
                                </div>
                                <div class="content">
                                    <h2>Hello {{FULL_NAME}},</h2>
                                    <p>Welcome to LuminoEd. You have successfully activated your profile and are now equipped with access to our advanced interactive ecosystem.</p>
                                    <p>Our platform bridges the gap between theory and execution by shifting your training from traditional flat screens into high-fidelity, spatial 3D environments. Whether you are scaling specialized technical competencies or mastering complex engineering structures, your curriculum is designed for complete, practical mastery.</p>
                                    <p><strong>What to expect from your workspace:</strong></p>
                                    <ul class="feature-list">
                                        <li class="feature-item"><strong>Interactive 3D Canvases:</strong> Inspect, manipulate, and explore high-fidelity models directly inside your web browser.</li>
                                        <li class="feature-item"><strong>Cross-Platform Flex:</strong> Transition smoothly between desktop web views and full standalone VR headsets.</li>
                                        <li class="feature-item"><strong>On-Demand Resuming:</strong> Jump right back into your active module exactly where you last closed your session.</li>
                                    </ul>
                                    <p>Your dashboard is ready and waiting for your first login initiative. Click below to explore your assigned courses and configure your simulation hardware preferences.</p>
                                    <div class="cta-container">
                                        <a href="https://www.luminoed.com/login" class="cta">Access Training Portal</a>
                                    </div>
                                </div>
                                <div class="footer">
                                    LuminoEd Enterprise | South Africa | <a href="https://www.luminoed.com/privacy">Privacy Policy</a> | <a href="https://luminoed.com/terms">Terms of Service</a><br><br>
                                    Have deployment questions or hardware compatibility inquiries? Our engineering support desk is always active. Just reply directly to this email.<br><br>
                                    © 2026 LuminoEd. All rights reserved.
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
                            <a href="https://www.luminoed.com/verify/password-reset" class="cta">Verify & Reset Password</a>
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
