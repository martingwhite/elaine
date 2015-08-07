function [TRANS_HAT,EMIS_HAT] = train(pathToData,numStates,numEmissions)
%pathToData = '.'; numStates = 2; numEmissions = 6;

% each trace is stored in a [1-9][0-9]*.dat
dataFiles = strcat(pathToData,'/*.dat')

% code for generating dummy data...
%{
TRANS = [0.90 0.10; 0.05 0.95];
EMIS = [1/6 1/6 1/6 1/6 1/6 1/6; 7/12 1/12 1/12 1/12 1/12 1/12];

for i = 1:2
    [seq,states] = hmmgenerate(100/i,TRANS,EMIS);
    data = [seq;states];
    fid = fopen(strcat(num2str(i),'.dat'),'w');
    fprintf(fid,'%d\t%d\n',data);
    fclose(fid);
end
%}

% glob the files containing the traces...
files = dir(dataFiles);
numFiles = num2str(length(files))

corpusEmissions = cell(length(files),1);
corpusStates = cell(length(files),1);

p = zeros(1,numStates);
TRGUESS = zeros(numStates);
EMITGUESS = zeros(numStates,numEmissions);

i = 1; % corpora are 1-indexed
for file = files'
    % assumes two columns of tab delimited ints...
    trace = load(file.name); % ...(1) emissions and (2) states
    firstEmission = trace(1,1);
    firstState = trace(1,2);
    p(firstState) = p(firstState) + 1; % initial state distribution
    EMITGUESS(firstState,firstEmission) = EMITGUESS(firstState,firstEmission) + 1;
    for j = 2:size(trace,1)
        currEmission = trace(j,1);
        prevState = trace(j-1,2);
        currState = trace(j,2);
        TRGUESS(prevState,currState) = TRGUESS(prevState,currState) + 1;
        EMITGUESS(currState,currEmission) = EMITGUESS(currState,currEmission) + 1;
    end
    trace = trace'; % 2-by-m; m is $ wc -l file; hmm utils operate on rows
    corpusEmissions{i} = trace(1,:);
    corpusStates{i} = trace(2,:);
    i = i + 1;
end

% either construct random TRANS and EMIS matrices...
%TRGUESS = rand(numStates); % rand \sim U[0,1]
%EMITGUESS = rand(numStates,numEmissions);

% ...or load ML estimates...
rowSum = sum(p,2);
p = bsxfun(@rdivide,p,rowSum)
rowSum = sum(TRGUESS,2);
TRGUESS = bsxfun(@rdivide,TRGUESS,rowSum) % TRGUESS is row stochastic
rowSum = sum(EMITGUESS,2);
EMITGUESS = bsxfun(@rdivide,EMITGUESS,rowSum)

% apply pseudotransitions and pseudoemissions accordingly...
[ESTTR,ESTEMIT] = hmmtrain(corpusEmissions,TRGUESS,EMITGUESS,...
    'Maxiterations',1000,'Tolerance',1e-6)

% changing the initial state distribution...
%p = repmat(1/numStates,1,numStates) % p \sim U[1,numStates]
TRANS_HAT = [0 p; zeros(size(ESTTR,1),1) ESTTR]
EMIS_HAT = [zeros(1,size(ESTEMIT,2)); ESTEMIT]
