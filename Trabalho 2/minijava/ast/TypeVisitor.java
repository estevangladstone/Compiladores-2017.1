package minijava.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minijava.SymbolTable;

public class TypeVisitor implements Visitor<SymbolTable<String>, String> {
	public List<String> erros = new ArrayList<>();
	
	SymbolTable<Classe> classes = new SymbolTable<>(null);
	Map<String, String> supertypes = new HashMap<>();

	String nomeCiclo;
	
	public boolean subtype(String t1, String t2) {
		if(t1.equals(t2)) return true; // reflexivo
		if(t2.equals("Object") && 
				!(t1.equals("int") || t1.equals("int[]") || t1.equals("boolean")))
			return true;
		if(t1.equals("null") && 
				!(t2.equals("int") || t2.equals("int[]") || t2.equals("boolean")))
			return true;
		String supt1 = supertypes.get(t1);
		if(t2.equals(supt1)) return true;
		if(supt1 == null) return false;
		return subtype(supt1, t2); // transitivo
	}

	@Override
	public String visit(Atrib no, SymbolTable<String> ctx) {
		String texp = no.exp.accept(this, ctx);
		String tvar = ctx.get(no.nome);
		if(tvar == null) {
			erros.add("variável " + no.nome + 
					  " na atribuição da linha " + no.lin + 
					  " não declarada");
			tvar = "int";
		}
		if(!subtype(texp, tvar))
			erros.add("tipos na atribuição da linha " + no.lin + " incompatíveis, lado esquerdo é " + tvar + " e lado direito é " + texp);
		return null;
	}

	@Override
	public String visit(Bloco no, SymbolTable<String> ctx) {
		for(Cmd c: no.cmds) {
			c.accept(this, ctx);
		}
		return null;
	}

	@Override
	public String visit(Div no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(tesq.equals("int") && tdir.equals("int")) {
			return "int";
		}
		if(!tesq.equals("int"))
			erros.add("lado esquerdo da divisão na linha " + no.lin + " é " + tesq + " e não número");
		if(!tdir.equals("int"))
			erros.add("lado direito da divisão na linha " + no.lin + " é " + tdir + " e não número");
		return "int";
	}

	@Override
	public String visit(If no, SymbolTable<String> ctx) {
		String tcond = no.cond.accept(this, ctx);
		if(!tcond.equals("boolean"))
			erros.add("condição do if na linha " + no.lin + " é " + tcond + " e não booleana");
		no.cthen.accept(this, ctx);
		no.celse.accept(this, ctx);
		return null;
	}

	@Override
	public String visit(Igual no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(subtype(tesq, tdir) || subtype(tdir, tesq)) {
			return "boolean";
		}
		erros.add("tipos na igualdade da linha " + no.lin + " incompatíveis, lado esquerdo é " + tesq + " e lado direito é " + tdir);
		return "boolean";
	}

	@Override
	public String visit(Menor no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(tesq.equals("int") && tdir.equals("int")) {
			return "boolean";
		}
		if(!tesq.equals("int"))
			erros.add("lado esquerdo da comparação na linha " + no.lin + " é " + tesq + " e não número");
		if(!tdir.equals("int"))
			erros.add("lado direito da comparação na linha " + no.lin + " é " + tdir + " e não número");
		return "boolean";
	}

	@Override
	public String visit(Mult no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(tesq.equals("int") && tdir.equals("int")) {
			return "int";
		}
		if(!tesq.equals("int"))
			erros.add("lado esquerdo da multiplicação na linha " + no.lin + " é " + tesq + " e não número");
		if(!tdir.equals("int"))
			erros.add("lado direito da multiplicação na linha " + no.lin + " é " + tdir + " e não número");
		return "int";
	}

	@Override
	public String visit(Soma no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(tesq.equals("int") && tdir.equals("int")) {
			return "int";
		}
		if(!tesq.equals("int"))
			erros.add("lado esquerdo da soma na linha " + no.lin + " é " + tesq + " e não número");
		if(!tdir.equals("int"))
			erros.add("lado direito da soma na linha " + no.lin + " é " + tdir + " e não número");
		return "int";
	}

	@Override
	public String visit(Sub no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(tesq.equals("int") && tdir.equals("int")) {
			return "int";
		}
		if(!tesq.equals("int"))
			erros.add("lado esquerdo da subtração na linha " + no.lin + " é " + tesq + " e não número");
		if(!tdir.equals("int"))
			erros.add("lado direito da subtração na linha " + no.lin + " é " + tdir + " e não número");
		return "int";
	}

	@Override
	public String visit(Num no, SymbolTable<String> ctx) {
		return "int";
	}

	@Override
	public String visit(Println no, SymbolTable<String> ctx) {
		no.exp.accept(this, ctx);
		return null;
	}

	@Override
	public String visit(Prog no, SymbolTable<String> ctx) {
		for(Classe cls: no.cls) {
			if(cls.nome.equals("Object") ||
			   cls.nome.equals(no.main) ||
			   classes.containsKey(cls.nome)) {
				erros.add("classe " + cls.nome + " redeclarada na linha " + cls.lin);
			} else classes.put(cls.nome, cls);
		}
		for(Classe cls: classes.values()) {
			nomeCiclo = cls.nome;
			cls.accept(this, new SymbolTable<String>(null));
		}
		for(Classe cls: classes.values()) {
			supertypes.put(cls.nome, cls.pai);
		}
		nomeCiclo = null;
		for(Classe cls: classes.values()) {
			cls.accept(this, new SymbolTable<String>(null));
		}
		no.cmd.accept(this, new SymbolTable<String>(null));
		return null;
	}

	@Override
	public String visit(Id no, SymbolTable<String> ctx) {
		String tid = ctx.get(no.nome);
		if(tid == null) {
			erros.add("variável " + no.nome + 
					  " usada na expressão da linha " + no.lin + 
					  " não declarada");
			tid = "int";
		}
		return tid;
	}

	@Override
	public String visit(Metodo no, SymbolTable<String> ctx) {
		SymbolTable<String> cmetodo = new SymbolTable<>(ctx);
		for(Var param: no.params) {
			if(!cmetodo.containsKey(param.nome)) {
				cmetodo.put(param.nome, param.tipo);
			} else {
				erros.add("parâmetro " + param.nome + " redeclarado na linha " + no.lin);
			}
		}
		for(Var param: no.vars) {
			if(!cmetodo.containsKey(param.nome)) {
				cmetodo.put(param.nome, param.tipo);
			} else {
				erros.add("variável " + param.nome + " redeclarada na linha " + no.lin);
			}
		}
		for(Cmd cmd: no.cmds) {
			cmd.accept(this, cmetodo);
		}
		String tret = no.ret.accept(this, cmetodo);
		if(!subtype(tret, no.tret))
			erros.add("tipo da expressão de retorno " + tret +
					" não bate com o tipo de retorno " + no.tret + 
					" declarado no método da linha " + no.lin);
		return null;
	}

	@Override
	public String visit(Chamada no, SymbolTable<String> ctx) {
		String trexp = no.obj.accept(this, ctx);
		Classe cls = classes.get(trexp);
		if(cls == null) {
			erros.add("objeto da chamada de método " + no.nome + " tem tipo " + trexp + " que não existe ou não é classe na linha " + no.lin);
			for(Exp arg: no.args) arg.accept(this, ctx);
			return "int";
		}
		int imetodo = cls.nmetodos.indexOf(no.nome);
		if(imetodo == -1) {
			erros.add("método " + no.nome + " não existe na classe " + trexp + " na linha " + no.lin);
			for(Exp arg: no.args) arg.accept(this, ctx);
			return "int";
		}
		Metodo proc = cls.pmetodos.get(imetodo);
		String[] tparams = new String[proc.params.size()];
		for(int i = 0; i < tparams.length; i++) {
			tparams[i] = proc.params.get(i).tipo;
		}
		String[] targs = new String[no.args.size()];
		for(int i = 0; i < targs.length; i++) {
			targs[i] = no.args.get(i).accept(this, ctx);
		}
		if(tparams.length != targs.length) {
			erros.add("aridade da chamada na linha " + no.lin + " não bate com a do método " + no.nome + " da classe " + trexp);
		} else {
			for(int i = 0; i < tparams.length; i++) {
				if(!subtype(targs[i], tparams[i])) {
					erros.add("tipo " + targs[i] + " do argumento " +
							i + " da chamada de método " + no.nome + " da classe " + trexp + " na linha " + no.lin + " não é compatível com o tipo " + tparams[i] + " do parâmetro");
				}
			}
		}
		return proc.tret;
	}

	@Override
	public String visit(New no, SymbolTable<String> ctx) {
		if(!classes.equals("Object") && classes.get(no.classe) == null) {
			erros.add("classe " + no.classe + " no construtor na linha " + no.lin + " não existe");
			return "Object";
		}
		return no.classe;
	}

	@Override
	public String visit(Null no, SymbolTable<String> ctx) {
		return "null";
	}

	@Override
	public String visit(Classe no, SymbolTable<String> ctx) {
		if(nomeCiclo != null) {
			if(!no.ncampos.isEmpty()) return null;
			if(!no.pai.equals("Object")) {
				if(no.pai == nomeCiclo) {
					erros.add("ciclo na hierarquia de classes entre " + no.nome + " e " + no.pai);
					no.pai = "Object";
				} else {
					Classe csup = classes.get(no.pai);
					if(csup == null) {
						erros.add("superclasse " + no.pai + " da classe " + no.nome + " não existe na linha " + no.lin);
					} else {
						csup.accept(this, ctx);
						no.ncampos.addAll(csup.ncampos);
						no.tcampos.addAll(csup.tcampos);
						no.nmetodos.addAll(csup.nmetodos);
						no.pmetodos.addAll(csup.pmetodos);
					}
				}
			}
			for(Var c: no.campos) {
				if(no.ncampos.indexOf(c.nome) != -1) {
					erros.add("campo " + c.nome + " redeclarado na linha " + c.lin);
				} else {
					no.ncampos.add(c.nome);
					no.tcampos.add(c.tipo);
				}
			}
			List<String> nmetodos = new ArrayList<>();
			List<Metodo> pmetodos = new ArrayList<>();
			for(Metodo m: no.metodos) {
				if(nmetodos.indexOf(m.nome) != -1) {
					erros.add("método " + m.nome + " redeclarado na linha " + m.lin);
				} else {
					nmetodos.add(m.nome);
					pmetodos.add(m);
				}
			}
			for(int i = 0; i < nmetodos.size(); i++) {
				if(no.nmetodos.indexOf(nmetodos.get(i)) == -1) {
					no.nmetodos.add(nmetodos.get(i));
					no.pmetodos.add(pmetodos.get(i));
				} else { // método existe em alguma superclasse, mantém índice
					no.pmetodos.set(no.nmetodos.indexOf(nmetodos.get(i)), pmetodos.get(i));
				}
			}
		} else {
			Classe csup = null;
			if(!no.pai.equals("Object")) csup = classes.get(no.pai);
			for(Metodo metodo: no.metodos) {
				if(no.pmetodos.indexOf(metodo) != -1 &&
						csup != null && csup.nmetodos.indexOf(metodo.nome) != -1) {
					// método redefinido
					Metodo old = csup.pmetodos.get(csup.nmetodos.indexOf(metodo.nome));
					if(old.params.size() != metodo.params.size()) {
						erros.add("tentando redefinir método " + metodo.nome + " na classe " + no.nome + " na linha " + metodo.lin + " com aridade diferente da original");
					} else {
						if(!subtype(metodo.tret, old.tret))
							erros.add("redefinição do método " + metodo.nome + " na classe " + no.nome + " tem tipo de retorno " + metodo.tret + " incompatível com o tipo original " + old.tret);
						for(int i = 0; i < metodo.params.size(); i++) {
							String told = old.params.get(i).tipo;
							String tnew = metodo.params.get(i).tipo;
							if(!told.equals(tnew)) // Regra de redefinição de Java
								erros.add("redefinição do método " + metodo.nome + " na classe " + no.nome + " tem o parâmetro " + i + " de tipo " + tnew + " incompatível com o tipo original " + told);
						}
					}
				}
			}
			SymbolTable<String> mctx = new SymbolTable<>(ctx);
			mctx.put("this", no.nome);
			for(int i = 0; i < no.ncampos.size(); i++) {
				mctx.put(no.ncampos.get(i), no.tcampos.get(i));
			}
			for(Metodo m: no.metodos) {
				if(no.pmetodos.indexOf(m) != -1)
					m.accept(this, mctx);
			}
		}
		return null;
	}

	@Override
	public String visit(This no, SymbolTable<String> ctx) {
		String tthis = ctx.get("this");
		if(tthis == null) {
			erros.add("this usado fora de um método na linha " + no.lin);
			return "Object";
		}
		return tthis;
	}

	@Override
	public String visit(Campo no, SymbolTable<String> ctx) {
		String tobj = no.obj.accept(this, ctx);
		Classe cls = classes.get(tobj);
		if(cls == null) {
			erros.add("objeto do acesso ao campo " + no.nome + " tem tipo " + tobj + " que não existe ou não é classe na linha " + no.lin);
			return "int";
		}
		int icampo = cls.ncampos.indexOf(no.nome);
		if(icampo == -1) {
			erros.add("campo " + no.nome + " não existe na classe " + tobj + " na linha " + no.lin);
			return "int";
		}
		return cls.tcampos.get(icampo);
	}

	@Override
	public String visit(Dif no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(subtype(tesq, tdir) || subtype(tdir, tesq))
			return "boolean";
		erros.add("tipos na desigualdade da linha " + no.lin + " incompatíveis, lado esquerdo é " + tesq + " e lado direito é " + tdir);
		return "boolean";
	}

	@Override
	public String visit(ELog no, SymbolTable<String> ctx) {
		String tesq = no.e1.accept(this, ctx);
		String tdir = no.e2.accept(this, ctx);
		if(!tesq.equals("boolean") || !tdir.equals("boolean"))
			erros.add("tipos no E lógico da linha " + no.lin + " incompatíveis, lado esquerdo é " + tesq + " e lado direito é " + tdir + " e ambos deveriam ser booleanos");
		return "boolean";
	}

	@Override
	public String visit(False no, SymbolTable<String> ctx) {
		return "boolean";
	}

	@Override
	public String visit(Indexa no, SymbolTable<String> ctx) {
		String tvet = no.vet.accept(this, ctx);
		if(!tvet.equals("int[]"))
			erros.add("expressão " + no.vet + " na atribuição da linha " + no.lin + " não é um vetor");
		String tind = no.ind.accept(this, ctx);
		if(!tind.equals("int"))
			erros.add("expressão " + no.vet + " na atribuição da linha " + no.lin + " é "+ no.ind +" e não um inteiro");
		return "int";
	}

	@Override
	public String visit(Length no, SymbolTable<String> ctx) {
		String texp = no.exp.accept(this, ctx);
		if(!texp.equals("int[]"))
			erros.add("expressão " + no.exp + " na atribuição da linha " + no.lin + " não é um vetor");
		return "int";
	}

	@Override
	public String visit(Nao no, SymbolTable<String> ctx) {
		String te = no.e.accept(this, ctx);
		if(!te.equals("boolean"))
			erros.add("expressão da negação na linha " + no.lin + " é " + te + " e não booleana");
		return "boolean";
	}

	@Override
	public String visit(Neg no, SymbolTable<String> ctx) {
		String te = no.e.accept(this, ctx);
		if(!te.equals("int"))
			erros.add("expressão na linha " + no.lin + " é " + te + " e não inteira");
		return "int";
	}

	@Override
	public String visit(True no, SymbolTable<String> ctx) {
		return "boolean";
	}

	@Override
	public String visit(Vetor no, SymbolTable<String> ctx) {
		String ttam = no.tam.accept(this, ctx);
		if(!ttam.equals("int"))
			erros.add("tamanho " + no.tam + " na declaração do vetor da linha " + no.lin + " não é inteiro");
		return "int[]";
	}

	@Override
	public String visit(AtribVetor no, SymbolTable<String> ctx) {
		String tvar = ctx.get(no.nome);
		if(tvar == null) {
			erros.add("variável " + no.nome + " na atribuição da linha " + no.lin + " não declarada");
			tvar = "int";
		}
		if(!tvar.equals("int[]"))
			erros.add("variável " + no.nome + " na atribuição da linha " + no.lin + " não é um vetor");
		String tind = no.ind.accept(this, ctx);
		if(!tind.equals("int"))
			erros.add("o indice na atribuição ao vetor "+ no.nome +"  na linha " + no.lin + " é " + tind + " e não inteiro");
		String trval = no.rval.accept(this, ctx);
		if(!trval.equals("int"))
			erros.add("tipos na atribuição da linha " + no.lin + " incompatíveis, lado direito é " + trval + " e não inteiro");
		return null;
	}

	@Override
	public String visit(While no, SymbolTable<String> ctx) {
		no.corpo.accept(this, ctx);
		String tcond = no.cond.accept(this, ctx);
		if(!tcond.equals("bool"))
			erros.add("condição do while na linha " + no.lin + " é " + tcond + " e não booleana");
		return null;
	}
}
