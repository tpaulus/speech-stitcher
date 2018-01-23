var gulp = require('gulp'),
    sass = require('gulp-sass'),
    filter = require('gulp-filter'),
    autoprefixer = require('gulp-autoprefixer'),
    uglify = require('gulp-uglify'),
    imagemin = require('gulp-imagemin'),
    pump = require('pump'),
    concat = require('gulp-concat');

var config = {
    stylesPath: 'styles/sass',
    jsPath: 'styles/js',
    localLibsPath: 'styles/libs',
    imagesPath: 'images',
    outputDir: 'assets'
};

// DEPENDENCIES
gulp.task('libs', ['bootstrap-css', 'bootstrap-js', 'jquery', 'font-awesome-css', 'font-awesome-js', 'sweet-alerts']);

gulp.task('bootstrap-css', function (cb) {
    pump([
        gulp.src('./node_modules/bootstrap/dist/css/*'),
        filter('**/*.min.*'),
        concat('bootstrap.css'),
        gulp.dest(config.outputDir + '/css')
    ], cb);
});

gulp.task('bootstrap-js', function (cb) {
    pump([
        gulp.src('./node_modules/bootstrap/dist/js/*'),
        filter('**/bootstrap.min.*'),
        gulp.src('./node_modules/popper.js/dist/umd/*'),
        filter('**/*.min.js'),
        gulp.dest(config.outputDir + '/js')
    ],cb);
});

gulp.task('font-awesome-css', function (cb) {
    pump([
        gulp.src(config.localLibsPath + '/fa/*'),
        filter('**/*.css'),
        concat("fontawesome.css"),
        gulp.dest(config.outputDir + '/css')
    ], cb);
});

gulp.task('font-awesome-js', function (cb) {
    pump([
        gulp.src(config.localLibsPath + '/fa/*'),
        filter('**/*.js'),
        uglify(),
        concat('fontawesome.js'),
        gulp.dest(config.outputDir + '/js')
    ], cb);
});


gulp.task('jquery', function (cb) {
    pump([
        gulp.src('./node_modules/jquery/dist/*'),
        filter('**/jquery.min.*'),
        gulp.dest(config.outputDir + '/js')
    ], cb);
});

gulp.task('sweet-alerts', function () {
    return gulp.src('./node_modules/sweetalert/dist/sweetalert.min.js')
        .pipe(gulp.dest(config.outputDir + '/js'));
});


// IMAGES
gulp.task('images', function () {
    return gulp.src(config.imagesPath + '/*')
        .pipe(imagemin())
        .pipe(gulp.dest(config.outputDir + '/images'))
});


// CSS
gulp.task('css', function (cb) {
    pump([
        gulp.src(config.stylesPath + '/**/*.scss'),
        sass({
            outputStyle: 'compressed',
            includePaths: [
                config.stylesPath,
                './node_modules/bootstrap/scss'
            ]
        }).on('error', sass.logError),
        autoprefixer(),
        gulp.dest(config.outputDir + '/css')
    ], cb);
});

// JS
gulp.task('js', ['libs'], function (cb) {
    pump([
            gulp.src(config.jsPath + '/*'),
            filter('**/*.js'),
            uglify(),
            concat("main.min.js"),
            gulp.dest(config.outputDir + '/js')
        ],
        cb
    );
});

// WATCHES
gulp.task('watch', function () {
    gulp.watch([config.stylesPath + '**/*.scss', config.stylesPath + '**/*.sass', config.stylesPath + '**/*.css'], ['css']);
    gulp.watch([config.jsPath + '**/*.js'], ['js']);
    gulp.watch([config.imagesPath + '/**/*'], ['images']);
});

// BUILD TASKS
gulp.task('build', ['images', 'css', 'js']);
gulp.task('default', ['build']);