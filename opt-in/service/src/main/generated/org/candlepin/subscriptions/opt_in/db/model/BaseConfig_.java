package org.candlepin.subscriptions.opt_in.db.model;

import java.time.OffsetDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BaseConfig.class)
public abstract class BaseConfig_ {

	public static volatile SingularAttribute<BaseConfig, Boolean> syncEnabled;
	public static volatile SingularAttribute<BaseConfig, OffsetDateTime> created;
	public static volatile SingularAttribute<BaseConfig, OptInType> optInType;
	public static volatile SingularAttribute<BaseConfig, OffsetDateTime> updated;

	public static final String SYNC_ENABLED = "syncEnabled";
	public static final String CREATED = "created";
	public static final String OPT_IN_TYPE = "optInType";
	public static final String UPDATED = "updated";

}

