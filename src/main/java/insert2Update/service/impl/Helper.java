package service.impl;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import model.Column;
import model.Insert;
import model.Statement;
import model.Target;
import model.Update;
import model.Value;
import service.Scanner;
import service.Type;

public class Helper {

	private Scanner scanner;
	private Type token;

	public Helper(Reader r) {
		scanner = new ScannerImpl(r);
	}

	public Statement parse(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass.equals(Insert.class)) {
			return insert2Statement(targetClass);
		}
		return null;
	}

	private Statement insert2Statement(Class<?> targetClass) {
		if (targetClass.equals(Update.class)) {
			return insert2Update();
		}
		return null;
	}

	private Update insert2Update() {
		nextToken();
		// TODO replace insert into
		Target target = target();
		Column[] cols = columns();
		checkKeywordByName("values");
		
		// TODO gestire caratteri tt in possibile valore
		Value[] vals = values();
		
		
		Update table = new Update(target, cols, vals);
		expect(Type.SEMICOLON);
		return table;
	}

	private Target target() {
		String schema = "";
		String table = "";

		if (token == Type.TEXT) {
			table = scanner.getInput().getSval();
			expect(Type.TEXT);
		}

		if (token == Type.DOT) {
			expect(Type.DOT);

			if (token == Type.TEXT) {
				schema = table;
				table = scanner.getInput().getSval();
				expect(Type.TEXT);
			}
		}

		Target target = new Target(schema, table);
		return target;
	}

	private Column[] columns() {
		List<Column> cols = new ArrayList<Column>();
		expect(Type.OPEN_BRACKET);
		while (token != Type.CLOSED_BRACKET) {
			cols.add(column());
			if (token != Type.CLOSED_BRACKET) {
				expect(Type.COMMA);
			}
		}
		expect(Type.CLOSED_BRACKET);
		return cols.toArray(new Column[0]);
	}

	private Column column() {
		String val = "";
		if (token == Type.TEXT) {
			val = scanner.getInput().getSval();
			expect(Type.TEXT);
		}
		Column col = new Column(val);
		return col;
	}

	private Value[] values() {
		List<Value> vals = new ArrayList<Value>();
		expect(Type.OPEN_BRACKET);
		while (token != Type.CLOSED_BRACKET) {
			vals.add(value());
			if (token != Type.CLOSED_BRACKET) {
				expect(Type.COMMA);
			}
		}
		expect(Type.CLOSED_BRACKET);
		return vals.toArray(new Value[0]);
	}

	private Value value() {
		String sval = "";
		if (token == Type.TEXT) {
			sval = scanner.getInput().getSval();
			expect(Type.TEXT);
		}
		Value val = new Value(sval);
		return val;
	}

	private void checkKeywordByName(String keyword) {
		if (!keyword.equalsIgnoreCase(scanner.getInput().getSval().trim())) {
			error("Syntax error: '" + scanner.getInput().getSval() + "'.");
		}
		expect(Type.TEXT);
	}

	private void expect(Type t) {
		if (token != t)
			error("Syntax error: expected token Type." + t);
		nextToken();
	}

	private void nextToken() {
		token = scanner.nextToken();
	}

	private void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}

}