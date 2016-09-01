package directory;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.User;

public class Authentication {
	
	protected ArrayList<User> userList;
	
	//Create Array List com lista de utilizadores
	//metodo para ler o ficheiro dos utilizadores e guardar
	
	public void createList(String fileName){
		
		// The name of the file to open.
        //String fName = "users.txt";

        // This will reference one line at a time
        String name = null;
        String pass = null;
        userList = new ArrayList<User>();

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((name = bufferedReader.readLine()) != null) {
            	pass = bufferedReader.readLine();
            	userList.add(new User(name, pass));
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
            System.exit(1);
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
            System.exit(1);
            // Or we could just do this: 
            // ex.printStackTrace();
        }catch(Exception ex){
        	System.out.println("Erro - "+ ex.getMessage());
        	System.exit(1);
        }
		
	}
	
	public void listarLista(){
		
		System.out.println("Utilizadores Registados");
		System.out.println("Total: " + userList.size());
		
		for(User s : userList){	
			System.out.println("Nome: "+s.getName());
			System.out.println("Pass: "+s.getPassword());
		}
	}
	
	//Verificar se o user se encontra na lista de users
	public boolean exist(User client){
		
		for(User su : userList){
			if(su.equals(client))
				return true;
		}
		return false;
	}
}