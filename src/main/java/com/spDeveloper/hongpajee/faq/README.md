The Design of A Very Advanced Forum

A person can say words. Each piece of words consists two parts: opinion and reasons. The opinion can be agree or disagree. 

Each piece of words replies to another piece of words, except the root words. The root words has a opinion which means nothing. 

If a piece of words has no replies, then it is considered as irrelevant and colored white.

With more agrees, the a piece of words goes greener. With more disagree, the color goes red. 

The darker the color, the more important the piece of words is.

However, nothing can safe the majority of the people from ignorance.   





The Design of Q&A Department

Intuition:

One fact is that anything displayed by the web site is public. This is because no one of the users can be trusted to hold any private message private. 

The other fact is that messages to the web site owner can be private. This is because the transmission can be properly secured and the web site owner will not release information against himself. 

Recall that the purpose of the application is to bring the web site owner close to the followers. Therefore, it is best to fully explore the usage of any private channels. 

Now the question is what can we do with the one way private channel of this application where the owner can hear private messages from the followers but the followers can only have public messages from the owner. 

First of all, the owner should be able to hear the private messages. The messages can from each registered user. The messages are not visible by anyone except the owner: even the sender can not see or edit it again. The messages are listed for the owner, with as much information as possible.

The owner now can decide when and how to deal with the private messages. There are different kinds of messages:

 1. curses: user curse the owner
 2. praises: user say tell the owner how much they love him or her
 3. suggestions: user want the owner to change
 4. constructive questions: user ask a valuable question
 5. negative questions: user question the meaning or righteous for the owner's choice
 
 Basically, there are four categories of private messages:
 
 Positive messages that require no explanation
 Positive messages that require explanation
 Negative messages that require no explanation
 Negative messages that require explanation
 
 For users who send negative messages, the owner must have the choice to revenge him. 
 For positive messages, the owner would love to share it with the public. However, before sharing a private message, the permission from the sender should be acquired. 
 For messages that need explanation, both the question and the explanation should be made public because there is no privacy for the explanation. 
 
 Therefore, the user should mark their message as OK/not OK to share before sending it to the owner. If the user choose not OK to share, then no explanation will be expected. 
 
 For positive messages, there are no problem share or not share it. For negative messages like a curse, a revenge is enough. However, some negative messages may require explanation, for example, some follower may believed in some rumors and they talk about the rumor in private messages that are marked not OK to share. In this case, the owner should respect the "not OK to share". The explanation should be in an independent article which is not part of the Q&A. 
 
 One more thing is about the administrators. The owner can trust some administrators with all or some of the private messages. The administrators then got chance to speak for the web site owner. The name of the speaker will appear together with the answers, therefore the administrator will take responsibility for his words. If the administrator behaved badly in the Q&A, the owner can revenge him by untrust him with the Q&A or remove him out of the administration. 
 
 Therefore there are roles related to the Q&A department: 
 1. NEW ROLE: ROLE_QAM: Q&A manager who receives the list of private messages. By default, it is the web site owner. 
 2. NEW ROLE: ROLE_QAA: administrator who is eligible to handle certain private messages redirected by the Q&A manager. By default, any administrator is QA administrator. 
 3. NEW ROLE: ROLE_QAU: users who is eligible to send private messages. By default, any user is QA user.
 
 new functionalities:
 1. /qa/qam/list
 2. /qa/qam/qau/assign/{user} 
 3. /qa/qam/qau/reassgin/{user}
 4. /qa/qam/qaa/assgin/{user} 
 5. /qa/qam/qaa/reassgin/{user}
 6. /qa/qam/dispatch?message={message}&qaa={qaa}
 7. /qa/qaa/reply/post_n_edit/{message}, //can hide a message, can post reply with no words, can only reply to those messages dispatched to the qaa.  
 8. /qa/qau/message/send, //can set a message OK/not OK to publish, can mark a message expect explanation or not.
 9. /qa/list //all messages that is replied. The order is open to edit
 10. /qa/qam/list/edit //decide the sequence of QAs, decide which to hide. 
 
 OO Design:
 
 Whisper extends Message{
 		
 	//content is String content
 	
 	
 }
 
 WhisperList{
 
 	
 
 }
 
 
 
 
 
 
 
 FAQ{
 	PinedList<Message> pinedMessages;
 	List<Message> messages;
 	-----------------------------
 	pin(message){
 		pinedMessages.pin(messages.remove(message));
 		
 	}
 	unpin(message){
 		messages.add(pinedMessages.unpin(message));
 	}
 	
 	//encapulate a list
 	add()
 	sort()
 }
 
 
 interface PinedList<T>{
 	void pin(T element)
 	T unpin(T element) 
 }
 
 Message{
 	UUID uuid
 	
 	UUID author;
 	Content content;
 	Long timestamp;
 	-------------------------
 	String getContent(){
 		return content.getContent();
 	}
 }
 
 article, paragraph, question, answer, reply, like
 
 interface Content;
 BooleanContent, LinesContent, StringContent, OpinionContent
 
 
 MessageRelationshipServcie{
 	getAgrees(message);
 	getDisagrees(message);
 	getReplyees(message);
 	reply(replyee, replyer)
 }  


