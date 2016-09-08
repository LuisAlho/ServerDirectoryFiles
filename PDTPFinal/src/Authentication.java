


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Authentication {
	
	protected ArrayList<User> userList;
	
	/*LE FICHEIRO COM OS UTILIZADORES E CRIA UM ARRAY COM OS MESMOS*/
	
	public void createList(String fileName){
		
		
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
           
        }catch(Exception ex){
        	System.out.println("Erro - "+ ex.getMessage());
        	System.exit(1);
        }
		
	}
	
	/* MOSTRA NO ECRA OS UTILIZADORES REGISTADOS */
	public void listarLista(){
		
		System.out.println("\nUtilizadores Registados");
		System.out.println("Total: " + userList.size());
		
		for(User s : userList){	
			System.out.println("\nNome: "+s.getName());
			System.out.println("Pass: "+s.getPassword());
		}
	}
	
	/* VERIFICA SE UM UTILIZADOR ESTA NA LISTA */
	public boolean exist(User client){
		
		for(User su : userList){
			if(su.equals(client))
				return true;
		}
		return false;
	}
}