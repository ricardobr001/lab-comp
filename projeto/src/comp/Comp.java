package comp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import ast.MetaobjectAnnotation;
import ast.Program;

public class Comp {

	public static void main( String []args ) {
		new Comp().run(args);
	}

	public void run( String []args ) {

		File file;
		if ( args.length < 1 ||  args.length > 2 )  {
			System.out.println("Usage:\n   comp input");
			System.out.println("input is the file or directory to be compiled");
			System.out.println("the output file will be created in the current directory");
		}
		else {

			numSourceFilesWithAnnotCEP = 0;
			int numSourceFiles = 0;
			shouldButWereNotList = new ArrayList<>();
			wereButShouldNotList = new ArrayList<>();
			wereButWrongLineList = new ArrayList<>();
			correctList = new ArrayList<>();
			numSourceFilesWithAnnotNCE = 0;



			PrintWriter outError;
			outError = new PrintWriter(System.out);

			PrintWriter report;
			FileOutputStream reportStream = null;
			try {
				reportStream = new FileOutputStream("report.txt");
			} catch ( FileNotFoundException  e) {
				outError.println("Could not create 'report.txt'");
				return ;
			}
			report = new PrintWriter(reportStream);



			file = new File(args[0]);
			if ( ! file.exists() || ! file.canRead() ) {
				String msg = "Either the file " + args[0] + " does not exist or it cannot be read";
				System.out.println(msg);
				outError.println("-1 : " + msg);
				outError.close();
				report.close();
				return ;
			}
			if ( file.isDirectory() ) {
				// compile all files in this directory
				File fileList[] = file.listFiles();
				ArrayList<Program> programList = new ArrayList<>();
				for ( File f : fileList ) {
					String filename = f.getName();
					int lastIndexDot = filename.lastIndexOf('.');
					String ext = filename.substring(lastIndexDot + 1);
					if ( ext.equalsIgnoreCase("ci") ) {
						numSourceFiles++;
						try {
							Program program = compileProgram(f, filename, outError);
							programList.add(program);
						} catch (RuntimeException e ) {
							System.out.println("Runtime exception");
						}
						catch (Throwable t) {
							t.printStackTrace();
							System.out.println("Throwable exception");
						}
					}
				}
				if ( numSourceFilesWithAnnotNCE == 0 && numSourceFilesWithAnnotCEP == 0 ) {
					printErrorList(outError, programList);
				}
				else {
					printReport(numSourceFiles, report);
				}

			}
			else {
				Program program = compileProgram(file, args[0], outError);
				if ( numSourceFilesWithAnnotNCE == 0 && numSourceFilesWithAnnotCEP == 0 ) {
					printErrorList(outError, program);
				}
				else {
					printReport(1, report);
				}

			}


			report.close();
			System.out.println("Cianeto compiler finished");
			/**
             // Just a test
             StatementAssert stat = new StatementAssert( new VariableExpr( new Variable("name", Type.intType)), 10, "operator '+' in '1 + 1 == 2'");
             PW pw = new PW();
             pw.set(outError);
             stat.genC(pw);
             outError.flush();

			 */

		}
	}

	private static void printErrorList(PrintWriter outError, ArrayList<Program> programList) {
		/*
		 * no annotation was used in the source codes
		 */
		for ( Program program : programList ) {
			printErrorList(outError, program);
		}
	}

	/**
	   @param outError
	   @param program
	 */
	private static void printErrorList(PrintWriter outError, Program program) {
		/*
		 * no annotation was used in the source codes
		 */
		for ( CompilationError error : program.getCompilationErrorList() ) {
			String s = error.getLineWithError();
			if ( s == null ) { s = ""; }
			outError.println("Error at line " + error.getLineNumber() + ", "
					+ error.getMessage() + "\n" + s );
		}
		outError.flush();
	}

	/**
	   @param numSourceFiles
	   @param report
	 */
	private void printReport(int numSourceFiles, PrintWriter report) {


		boolean compilerOk = true;
		report.println("Relatório do Compilador");
		report.println();
		if ( numSourceFilesWithAnnotCEP > 0 ) {
			report.println(this.shouldButWereNotList.size() + " de um total de " + numSourceFilesWithAnnotCEP +
					" erros que deveriam ser sinalizados não o foram (" +
					(int ) (100.0*this.shouldButWereNotList.size()/this.numSourceFilesWithAnnotCEP) + "%)");
			report.println(this.wereButWrongLineList.size() + " erros foram sinalizados na linha errada ("
					+ (int ) (100.0*this.wereButWrongLineList.size()/this.numSourceFilesWithAnnotCEP) + "%)");
		}
		if ( numSourceFiles -  numSourceFilesWithAnnotCEP != 0 ) {
			report.println(this.wereButShouldNotList.size() +
					" erros foram sinalizados em " + (numSourceFiles -  numSourceFilesWithAnnotCEP)
					+ " arquivos sem erro (" +
					(int ) (100.0*this.wereButShouldNotList.size()/(numSourceFiles -  numSourceFilesWithAnnotCEP)) + "%)"
					);
		}

		if ( numSourceFilesWithAnnotCEP > 0 ) {
			if ( shouldButWereNotList.size() == 0 ) {
				report.println("Todos os erros que deveriam ter sido sinalizados o foram");
			}
			else {
				compilerOk = false;
				report.println();
				report.println("Erros que deveriam ser sinalizados mas não foram:");
				report.println();
				for (String s : this.shouldButWereNotList) {
					report.println(s);
					report.println();
				}
			}

			if ( wereButWrongLineList.size() == 0 ) {
				report.println("Um ou mais arquivos de teste tinham erros, mas estes foram sinalizados nos números de linhas corretos");
			}
			else {
				compilerOk = false;
				report.println("######################################################");
				report.println("Erros que foram sinalizados na linha errada:");
				report.println();
				for (String s : this.wereButWrongLineList) {
					report.println(s);
					report.println();
				}

			}

		}
		if ( numSourceFiles -  numSourceFilesWithAnnotCEP != 0  ) {
			if ( wereButShouldNotList.size() == 0 ) {
				report.println("O compilador não sinalizou nenhum erro que não deveria ter sinalizado");
			}
			else {
				compilerOk = false;
				report.println("######################################################");
				report.println("Erros que foram sinalizados mas não deveriam ter sido:");
				report.println();
				for (String s : this.wereButShouldNotList) {
					report.println(s);
					report.println();
				}
			}
		}

		if ( correctList.size() > 0 ) {
			report.println("######################################################");
			report.print("Em todos os testes abaixo, o compilador sinalizou o erro na linha correta (quando o teste tinha erros) ");
			report.print("ou não sinalizou o erro (quando o teste NÃO tinha erros). Mas é necessário conferir se as ");
			report.print("mensagens emitidas pelo compilador são compatíveis com as mensagens de erro sugeridas pelas chamadas aos ");
			report.print("metaobjetos dos testes. ");
			report.println();
			report.println();
			report.println("A lista abaixo contém o nome do arquivo de teste, a mensagem que ele sinalizou e a mensagem sugerida pelo arquivo de teste");
			report.println();
			for (String s : this.correctList ) {
				report.println(s);
				report.println();
			}
		}
		if ( compilerOk ) {
			if ( numSourceFiles == 1 )
				report.println("Para o caso de teste que você utilizou, o compilador está correto");
			else
				report.println("Para os casos de teste que você utilizou, o compilador está correto");

		}

	}

	/**
	   @param args
	   @param stream
	   @param numChRead
	   @param outError
	   @param printWriter
	 * @throws IOException
	 */
	private Program compileProgram(File file, String filename, PrintWriter outError)  {
		Program program;
		FileReader stream;
		int numChRead;

		try {
			stream = new FileReader(file);
		} catch ( FileNotFoundException e ) {
			String msg = "Something wrong: file does not exist anymore";
			outError.println(msg);
			return null;
		}
		// one more character for '\0' at the end that will be added by the
		// compiler
		char []input = new char[ (int ) file.length() + 1 ];

		try {
			numChRead = stream.read( input, 0, (int ) file.length() );
			if ( numChRead != file.length() ) {
				outError.println("Read error in file " + filename);
				stream.close();
				return null;
			}
			stream.close();
		} catch ( IOException e ) {
			String msg = "Error reading file " + filename;
			outError.println(msg);
			try { stream.close(); } catch (IOException e1) { }
			return null;
		}


		Compiler compiler = new Compiler();


		program = null;
		// the generated code goes to a file and so are the errors
		program  = compiler.compile(input, outError );
		callMetaobjects(filename, program, outError);

		return program;
		/*
           if ( ! program.hasCompilationErrors() ) {

               String outputFileName;

               int lastIndex;
               if ( (lastIndex = filename.lastIndexOf('.')) == -1 )
                  lastIndex = filename.length();
               outputFileName = filename.substring(0, lastIndex);
               if ( (lastIndex = filename.lastIndexOf('\\')) != -1 )
            	   outputFileName = outputFileName.substring(lastIndex + 1);



               FileOutputStream  outputStream;
               try {
            	   outputFileName = outputFileName + ".java";
                  outputStream = new FileOutputStream(outputFileName);
               } catch ( IOException e ) {
                   String msg = "File " + outputFileName + " was not found";
                   outError.println(msg);
                   return ;
               }
               PrintWriter printWriter = new PrintWriter(outputStream);


              PW pw = new PW();
              pw.set(printWriter);
              program.genJava( pw );
              if ( printWriter.checkError() ) {
                 outError.println("There was an error in the output");
              }
              printWriter.close();
           }
		 */
	}



	public void callMetaobjects(String filename, Program program, PrintWriter outError) {

		boolean foundCE = false;
		boolean foundNCE = false;
		for ( MetaobjectAnnotation annot : program.getMetaobjectCallList() ) {
			if ( annot.getName().equals("cep") ) {
				this.numSourceFilesWithAnnotCEP++;

				String message = (String ) annot.getParamList().get(2);
				int lineNumber = (Integer ) annot.getParamList().get(0);
				if ( ! program.hasCompilationErrors() ) {
					// there was no compilation error. There should be no call @ce(...)
					// the source code, through calls to "@ce(...)", informs that
					// there are errors
					String whatToCorrect = "";
					if ( annot.getParamList().size() >= 4 ) {
						whatToCorrect = (String ) annot.getParamList().get(3);
						whatToCorrect = " (" + whatToCorrect + ")";
					}
					this.shouldButWereNotList.add(filename + ", " + lineNumber + ", " + message +
							whatToCorrect
							);
					if ( foundCE )
						outError.println("More than one 'ce' metaobject calls in the same source file '" + filename + "'");
					foundCE = true;
				}
				else {
					// there was a compilation error. Check it.
					int lineOfError = program.getCompilationErrorList().get(0).getLineNumber();
					String ceMessage = (String ) annot.getParamList().get(2);
					String compilerMessage = program.getCompilationErrorList().get(0).getMessage();
					if ( lineNumber != lineOfError ) {

						String whatToCorrect = "";
						if ( annot.getParamList().size() >= 4 ) {
							whatToCorrect = (String ) annot.getParamList().get(3);
							whatToCorrect = "(" + whatToCorrect + ")";
						}


						this.wereButWrongLineList.add(filename + "\n" +
								"    correto:    " + lineNumber + ", " + ceMessage + " " + whatToCorrect + "\n" +
								"    sinalizado: " + lineOfError + ", " + compilerMessage);
					}
					else {
						// the compiler is correct. Add to correctList the message
						// that the compiler signalled and the message of the test, given in @ce
						correctList.add(filename + "\r\n" +
								"The compiler message was: \"" + compilerMessage + "\"\r\n" +
								"The 'ce' message is:      \"" + ceMessage + "\"\r\n" );
					}
				}
			}
			else if ( annot.getName().equals("nce") ) {
				++this.numSourceFilesWithAnnotNCE;
				if ( foundNCE )
					outError.println("More than one 'nce' metaobject calls in the same source file '" + filename + "'");
				foundNCE = true;
				if ( program.hasCompilationErrors() ) {
					int lineOfError = program.getCompilationErrorList().get(0).getLineNumber();
					String message = program.getCompilationErrorList().get(0).getMessage();
					this.wereButShouldNotList.add(filename + ", " + lineOfError + ", " + message);
				}
			}
		}
		if ( foundCE && foundNCE )
			outError.println("Calls to metaobjects 'ce' and 'nce' in the same source code '" + filename + "'");

	}

	ArrayList<String> shouldButWereNotList, wereButShouldNotList, wereButWrongLineList, correctList;

	/**
	 * number of tests with errors. That is, the number of tests in which there is an metaobject annotation  {@literal @}cep.
	 */
	private int	numSourceFilesWithAnnotCEP;

	/**
	 * number of source files compiled in which there is an annotation 'nce'
	 */
	private int numSourceFilesWithAnnotNCE;


}
