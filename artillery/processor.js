const fs = require('fs');
const path = require('path');
const imagePath = path.join(__dirname, 'test-image.jpg');
let imageBuffer;


try {
imageBuffer = fs.readFileSync(imagePath);
console.log(`✅ 테스트 이미지 로드 완료: ${(imageBuffer.length / 1024 / 1024).toFixed(2)} MB`);
} catch (e) {
    console.error("🚨 테스트 이미지(test-image.jpg)를 찾을 수 없습니다!", e);
    process.exit(1);
}

module.exports = {
    generateRandomData: function (requestParams, context, ee, next) {
    context.vars = context.vars || {};

    const requestId = Math.floor(Math.random() * 1000000);

    context.vars.filename = `test_file_${requestId}.jpg`;
    context.vars.realImageBuffer = imageBuffer;
    context.vars.imageSize = imageBuffer.length;
    return next();
    }
};