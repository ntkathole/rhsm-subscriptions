package org.candlepin.subscriptions.opt_in.db.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AccountConfig.class)
public abstract class AccountConfig_ extends org.candlepin.subscriptions.opt_in.db.model.BaseConfig_ {

	public static volatile SingularAttribute<AccountConfig, Boolean> reportingEnabled;
	public static volatile SingularAttribute<AccountConfig, String> accountNumber;

	public static final String REPORTING_ENABLED = "reportingEnabled";
	public static final String ACCOUNT_NUMBER = "accountNumber";

}

