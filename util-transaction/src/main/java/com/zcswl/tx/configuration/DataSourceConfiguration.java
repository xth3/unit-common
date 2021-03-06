package com.zcswl.tx.configuration;

import com.zcswl.tx.DataSourceContextHolder;
import com.zcswl.tx.DataSourceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>注册当前的读库和写库
 *
 * <p>TODO 基于mybatis的数据，Mapper映射
 *
 * @author zhoucg
 * @date 2019-12-30 15:30
 */
@Configuration
public class DataSourceConfiguration {

    @Lazy
    @Bean("dataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }

    @Lazy
    @Bean("readDataSource")
    @ConfigurationProperties("spring.datasource.read")
    public DataSource readDataSource() {
        DataSource dataSource = DataSourceBuilder.create().build();
        return dataSource;
    }

    @Lazy
    @Bean("routingDataSource")
    @Primary
    public AbstractRoutingDataSource routingDataSource(@Qualifier("dataSource") DataSource dataSource , @Qualifier("readDataSource") DataSource readDataSource ) {

        AbstractRoutingDataSource proxy =  new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DataSourceContextHolder.get();
            }
        };
        Map<Object,Object> map = new HashMap<>();
        map.put(DataSourceType.READ,readDataSource);
        map.put(DataSourceType.WRITE,dataSource);
        proxy.setTargetDataSources(map);
        proxy.setDefaultTargetDataSource(dataSource);

        return proxy;
    }

    @Bean
    public PlatformTransactionManagerCustomizer platformTransactionManagerCustomizer(AbstractRoutingDataSource abstractRoutingDataSource){
        return transactionManager -> {
            DataSourceTransactionManager dataSourceTransactionManager = (DataSourceTransactionManager) transactionManager;
            dataSourceTransactionManager.setDataSource(abstractRoutingDataSource);
        };
    }
}
