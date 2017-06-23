import java.io.*;
import java.util.List;

import minijava.*;

class Testador{
    public static void main ( String[] args ) throws Exception{
        FileReader f = new FileReader( args[0] );
        Scanner scanner = new Scanner( f );
        List<Token> tokens = scanner.getTokens();
        
        System.out.println("Tokens de " + args[0] + " :");
        for( Token tok : tokens ){
            System.out.println( tok.toString() );
        }
    }
}

