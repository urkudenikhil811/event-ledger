function fn() {
    var env = karate.env || 'sit';
    karate.log('running against environment:', env);

    var config = {
        env: env,
        gatewayUrl: 'http://localhost:8081',
        accountUrl: 'http://localhost:8080'
    };

    if (env === 'uat') {
        config.gatewayUrl = 'http://localhost:9081';
        config.accountUrl = 'http://localhost:9080';
    }

    return config;
}