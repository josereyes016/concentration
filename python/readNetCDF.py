from netCDF4 import Dataset
import matplotlib.pyplot as plt
import numpy as np

# Open a NetCDF file
dataset = Dataset('concentration.timeseries.nc', 'r')

# List all variables in the dataset
#print(dataset.variables.keys())

#print(dataset)
time_index = 8
#print("Dimensions:", dataset.dimensions)

#print("Variables:", dataset.variables)

# Access a specific variable
concentration = dataset.variables['concentration']
x_vals = dataset.variables['x'][:]
y_vals = dataset.variables['y'][:]

# Read data from the variable
concentration_data = concentration[time_index, 0,:,:]
c_vals = []
#print(concentration_data)

for x in range(36):
    for y in range(26):
        c_vals.append(concentration_data[y, x])
        #print(x_vals[x], y_vals[y], concentration_data[y, x])

xm, ym = np.meshgrid(x_vals, y_vals)

dataset.close()

plt.figure(figsize=(10,7))
plt.contourf(x_vals, y_vals, concentration_data)
plt.savefig('time_' + str(time_index) + '_plot.png')
plt.show()

#print(concentration_data)

# Access variable metadata (e.g., dimensions, attributes)
#print(concentration.dimensions)
#print(concentration.units)

# Close the dataset
