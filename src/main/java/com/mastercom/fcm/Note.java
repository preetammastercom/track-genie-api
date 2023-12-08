package com.mastercom.fcm;



import org.springframework.stereotype.Component;

@Component
public class Note {

	private String subject;
	private String content;

	public Note() {
		super();
		
	}

	public Note(String subject, String content) {
		super();
		this.subject = subject;
		this.content = content;
	}

	@Override
	public String toString() {
		return "Note [subject=" + subject + ", content=" + content + "]";
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
	
}
