package org.experiments.workload;

import org.experiments.logger.Cache;

public class FalseWorkload implements BaseWorkload {
    private final Cache cache;
    private final String query;
    public FalseWorkload(Cache cache, String query) {
        this.cache = cache;
        this.query = query;
    }

    @Override
    public void run() {
        cache.storeQuery(query, "False");
    }
}
