
package com.demo.api.configuration.multitenancyConfig.repository;

import com.demo.api.configuration.multitenancyConfig.model.MasterTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterTenantRepository
        extends JpaRepository<MasterTenant, Long> {

    /**
     * Using a custom named query
     * @param tenantId
     * @return
     */
    MasterTenant findByTenantId(String tenantId);
}
