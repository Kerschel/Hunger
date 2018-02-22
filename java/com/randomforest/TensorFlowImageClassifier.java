/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.sachinrajkumar.randomforest;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * A classifier specialized to label images using TensorFlow.
 */
public class TensorFlowImageClassifier {
    private static final String TAG = "ImageClassifier";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 2;
    private static final float THRESHOLD = 0.2f;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    private float[] floatValues;
    private float[] outputs;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    public TensorFlowImageClassifier(AssetManager assetManager, String modelFilename, String labelFilename, int inputSize, int imageMean, float imageStd, String inputName, String outputName) {
        System.out.println(assetManager);
        this.inputName = inputName;
        this.outputName = outputName;


        BufferedReader br = null;
        try {
            System.out.println("HIs");
            br = new BufferedReader(new InputStreamReader(assetManager.open(labelFilename)));
            String line;
            System.out.println("HI");
            while ((line = br.readLine()) != null) {
                this.labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error here : "+ e.toString());
            throw new RuntimeException("Problem reading label file!", e);

        }

        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        final Operation operation = this.inferenceInterface.graphOperation(outputName);
        final int numClasses = (int) operation.output(0).shape().size(1);
        Log.i(TAG, "Read " + this.labels.size() + " labels, output layer size is " + numClasses);

        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        this.inputSize = inputSize;
        this.imageMean = imageMean;
        this.imageStd = imageStd;

        // Pre-allocate buffers.
        this.outputNames = new String[]{outputName};
        this.intValues = new int[inputSize * inputSize];
        this.floatValues = new float[inputSize * inputSize * 3];
        this.outputs = new float[numClasses];

    }


    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        try {
            bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];
                floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
                floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
                floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
            }
            Trace.endSection();

            // Copy the input data into TensorFlow.
            Trace.beginSection("feed");
            inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
            Trace.endSection();

            // Run the inference call.
            Trace.beginSection("run");
            inferenceInterface.run(outputNames, logStats);
            Trace.endSection();

            // Copy the output Tensor back into the output array.
            Trace.beginSection("fetch");
            inferenceInterface.fetch(outputName, outputs);
            Trace.endSection();
        }
        catch (Exception e){
            System.out.println("JDSOJ" + e);
        }
        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        System.out.println("putputs:" + outputs.length);
        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > THRESHOLD) {
                pq.add(
                        new Recognition(
                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
            }
        }
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        System.out.println("IUHSGB:" + pq.size());
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        System.out.println("Regcis:" + recognitionsSize);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }
}
