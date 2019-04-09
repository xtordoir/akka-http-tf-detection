

### Default config
export MODEL_BASEDIR=/tmp
export MODEL_NAME="ssd_mobilenet_v2_coco_2018_03_29"
export MODEL_FROZENGRAPHFILENAME="frozen_inference_graph.pb"
export MODEL_BASEURL="http://download.tensorflow.org/models/object_detection/"

export MODEL_MAPFILENAME="mscoco_label_map.pbtxt"
export MODEL_MAPBASEURL="https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/"


### Example of config based on custom location for models
export MODEL_BASEDIR=$HOME/data/models/tmp
export MODEL_NAME="ssd_mobilenet_v2_coco_2018_03_29"
export MODEL_FROZENGRAPHFILENAME="frozen_inference_graph.pb"
export MODEL_BASEURL="http://download.tensorflow.org/models/object_detection/"

export MODEL_MAPFILENAME="mscoco_label_map.pbtxt"
export MODEL_MAPBASEURL="https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/"


### HOME Anonymizer Config, models were pre-downloaded to target location
export MODEL_BASEDIR=$HOME/data/models/tmp
export MODEL_NAME="anonymizer"
export MODEL_FROZENGRAPHFILENAME="weights_face_v1.0.0.pb"

export MODEL_MAPFILENAME="mscoco_label_map.pbtxt"
export MODEL_MAPBASEURL="https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/data/"
