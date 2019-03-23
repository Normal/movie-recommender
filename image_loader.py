from google_images_download import google_images_download
response = google_images_download.googleimagesdownload()

def get_image(kw, id):
    arguments = {"keywords": kw, "limit": 1, "output_directory": "/Users/normal/dev/als-spark/data/images", "image_directory": id}
    absolute_image_paths = response.download(arguments)
    return absolute_image_paths

import csv
with open('/Users/normal/dev/als-spark/data/ml-latest-small/movies.csv', newline='') as csvfile:
    reader = csv.reader(csvfile, delimiter=',', quotechar='"')
    for row in reader:
        path = get_image(row[1], row[0])
        print(path)