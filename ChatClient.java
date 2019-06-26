import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Date;
import java.text.*;
import java.util.Iterator;

class ChatClient extends Thread
{
	protected int serverPort = 1234;
        
        static String username;
        static String writingChannel = "RKchat";
        
	public static void main(String[] args) throws Exception {
		new ChatClient();
	}
        
	public ChatClient() throws Exception {
		Socket socket = null;
		DataInputStream in = null;
		DataOutputStream out = null;
                
		// connect to the chat server
		try {   Scanner sc = new Scanner(System.in);
                
                        boolean zeObstaja = false;
                        do{
                            
                            zeObstaja = false;
                            System.out.print("Enter your username: ");
                            username = sc.nextLine();
                            
                            Iterator <String> i = ChatServer.usernames.iterator();
                            while(i.hasNext()){
                                if(i.equals(username)){
                                    zeObstaja = true;
                                    break;
                                }
                            }
                        } while (zeObstaja);
                        
			System.out.println("[system] connecting to chat server ...");
			socket = new Socket("localhost", serverPort); // create socket connection
			in = new DataInputStream(socket.getInputStream()); // create input stream for listening for incoming messages
			out = new DataOutputStream(socket.getOutputStream()); // create output stream for sending messages
			System.out.println("[system] connected");
                        
			ChatClientMessageReceiver message_receiver = new ChatClientMessageReceiver(in); // create a separate thread for listening to messages from the chat server
			message_receiver.start(); // run the new thread
                        
                        out.writeUTF(username);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// read from STDIN and send messages to the chat server
		BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("-----------------------------------");
                System.out.printf("Current writing channel: [%s]\n", writingChannel);
                System.out.println("-----------------------------------");
		String userInput;
                
		while ((userInput = std_in.readLine()) != null) { // read a line from the console
                    
                    if(userInput.startsWith("@")){ // @receiver  <-- change writing channel (receiver = any username)
                        writingChannel = userInput.substring(1, userInput.length());
                        System.out.println("-----------------------------------");
                        System.out.printf("Current writing channel: [%s]\n", writingChannel);
                        System.out.println("-----------------------------------");
                    } else{
                        this.sendMessage(userInput, out); // send the message to the chat server
                    }
                    Thread.sleep(5);
		}
		// cleanup
		out.close();
		in.close();
		std_in.close();
		socket.close();
	}

	private void sendMessage(String message, DataOutputStream out) {
		try {
                        out.writeUTF(username); // send the username to the chat server
			out.writeUTF(message);
                        Date now = new Date();
                        SimpleDateFormat f = new SimpleDateFormat("'('HH:mm')' ");
                        out.writeUTF(f.format(now));
                        out.writeUTF(writingChannel);
			out.flush(); // ensure the message has been sent
                        System.out.printf("[%s] %s%s said: %s\n", writingChannel, f.format(now), "YOU", message.toUpperCase());
		} catch (IOException e) {
			System.err.println("[system] could not send message");
			e.printStackTrace(System.err);
		}
	}
}

// wait for messages from the chat server and print the out
class ChatClientMessageReceiver extends Thread {
	private DataInputStream in;

	public ChatClientMessageReceiver(DataInputStream in) {
		this.in = in;
	}

	public void run() {
		try {
                    String sender;
                    String message;
                    while ((sender = this.in.readUTF()) != null && (message = this.in.readUTF()) != null) { // read new message
                           
                        System.out.printf("[%s] %s\n", sender, message);
                        if(message.startsWith("Can't find")){
                                
                            ChatClient.writingChannel = "RKchat";
                            System.out.println("-----------------------------------");
                            System.out.printf("Current writing channel: [%s]\n", ChatClient.writingChannel);
                            System.out.println("-----------------------------------");
                        }
                    }
                        
		} catch (Exception e) {
			System.err.println("[system] could not read message");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
