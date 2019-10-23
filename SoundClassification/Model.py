import numpy as np
from keras.layers import Conv2D, Activation, Dropout, Flatten, Dense, MaxPooling2D, BatchNormalization, LeakyReLU , GlobalAveragePooling2D
from keras.models import Sequential
import keras.backend as K
K.set_image_data_format('channels_last')
import matplotlib.pyplot as plt
from matplotlib.pyplot import imshow
from sklearn import metrics

#defining parameters
BATCH_SIZE = 256
EPOCHS = 90
n_rows, n_columns, n_channels = 40, 174, 1

#loading and reshaping the train and test data sets
train_x = np.load('train_x_cnn.npy')
train_y = np.load('train_y_cnn.npy')
test_x = np.load('test_x_cnn.npy')
test_y = np.load('test_y_cnn.npy')

train_x = train_x.reshape(train_x.shape[0], n_rows, n_columns, n_channels)
test_x = test_x.reshape(test_x.shape[0], n_rows, n_columns, n_channels)

#model architecture
model =  Sequential()
"""==========================================="""
model.add(Conv2D(16, kernel_size=2, input_shape=(n_rows, n_columns, n_channels))) #(40,174,32)
model.add(Dropout(0.2))
model.add(LeakyReLU(alpha=0.1))
#model.add(BatchNormalization())
model.add(MaxPooling2D(pool_size=2)) #(20,87,32)
"""==========================================="""
model.add(Conv2D(32, kernel_size=2)) #(20,87,64)
model.add(Dropout(0.2))
model.add(LeakyReLU(alpha=0.1))
#model.add(BatchNormalization())
model.add(MaxPooling2D(pool_size=2)) #(10,43,64)

"""==========================================="""
model.add(Conv2D(64, kernel_size=2)) #(10,43,128)
model.add(Dropout(0.2))
model.add(LeakyReLU(alpha=0.1))
#model.add(BatchNormalization())
model.add(MaxPooling2D(pool_size=2, strides=2, padding='same')) #(5,21,128)
"""==========================================="""
model.add(Conv2D(128, kernel_size=2)) #(5,21,256)
model.add(Dropout(0.2))
model.add(LeakyReLU(alpha=0.1))
#model.add(BatchNormalization())
model.add(MaxPooling2D(pool_size=2)) #(2,10,256)
"""==========================================="""
model.add(GlobalAveragePooling2D())
#model.add(Flatten()) #5120
#model.add(Dense(512, activation="relu"))
#model.add(Dropout(0.5))
model.add(Dense(10, activation="softmax"))
"""==========================================="""

# Compile model and visualising the summary of model
model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=['accuracy'])
model.summary()

# Train model
history = model.fit(train_x, train_y,
          batch_size=BATCH_SIZE,
          epochs=EPOCHS,
          verbose=1,
          validation_data=(test_x, test_y))

#saving the model 
model.save('my_model3.h5') 

# summarize history for accuracy
plt.plot(history.history['accuracy'])
plt.plot(history.history['val_accuracy'])
plt.title('model accuracy')
plt.ylabel('accuracy')
plt.xlabel('epoch')
plt.legend(['train', 'val'], loc='upper left')
plt.show()
# summarize history for loss
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()

print('**********************************')
training_score = model.evaluate(train_x, train_y, verbose=0)
print('Training Accuracy: ', training_score)
print('**********************************')
test_score = model.evaluate(test_x, test_y, verbose=0)
print('Test Accuracy: ', test_score)
print('**********************************')
predictions = model.predict(test_x,batch_size=64, verbose=0)
np.save('Predictions3.npy', predictions)