//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blossom.project.ec.db.util;

import blossom.project.ec.db.config.DataSourceType;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;

public class SQLParserUtil {
    public SQLParserUtil() {
    }

    public static DataSourceType parseSQLType(String sql) {
        if (CommonUtil.isNullOrEmpty(sql)) {
            return DataSourceType.READ;
        } else {
            Lexer lexer = new Lexer(sql);

            Token tok;
            do {
                lexer.nextToken();
                tok = lexer.token();
                if (Token.INSERT.equals(tok) || Token.UPDATE.equals(tok) || Token.DELETE.equals(tok) || Token.CREATE.equals(tok) || Token.ALTER.equals(tok) || Token.DROP.equals(tok) || Token.SET.equals(tok)) {
                    return DataSourceType.WRITE;
                }

                if (Token.SELECT.equals(tok)) {
                    return DataSourceType.READ;
                }
            } while(tok != Token.EOF);

            return DataSourceType.READ;
        }
    }
}
