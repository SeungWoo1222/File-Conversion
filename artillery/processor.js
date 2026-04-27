module.exports = {
  generateRandomData: function (requestParams, context, ee, next) {
    context.vars = context.vars || {};
    const requestId = Math.floor(Math.random() * 1000000);
    context.vars.filename = `test_file_${requestId}.jpg`;
    return next();
    }
};