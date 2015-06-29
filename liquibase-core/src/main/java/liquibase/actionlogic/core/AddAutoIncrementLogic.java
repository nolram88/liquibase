package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class AddAutoIncrementLogic extends AbstractSqlBuilderLogic<AddAutoIncrementAction> {

    public static enum Clauses {
        autoIncrementDefinition, dataType
    }

    @Override
    protected Class<AddAutoIncrementAction> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsAutoIncrement();
    }

    @Override
    public ValidationErrors validate(AddAutoIncrementAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField("columnName", action);
        validationErrors.checkForRequiredField("columnDataType", action);

        if (!validationErrors.hasErrors()) {
            if (action.columnName.asList().size() < 2) {
                validationErrors.addError("Table name is required");
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(AddAutoIncrementAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)));
    }

    protected StringClauses generateSql(AddAutoIncrementAction action, Scope scope) {

        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses();
        clauses.append(Clauses.dataType, DataTypeFactory.getInstance().fromDescription(action.columnDataType + "{autoIncrement:true}", database).toDatabaseDataType(database).toSql());
        clauses.append(Clauses.autoIncrementDefinition, database.getAutoIncrementClause(action.startWith, action.incrementBy));

        return clauses;
    }
}